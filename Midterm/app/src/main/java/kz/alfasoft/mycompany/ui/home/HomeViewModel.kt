package kz.alfasoft.mycompany.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kz.alfasoft.mycompany.core.AppClock
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.data.model.LeaveBalance
import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.model.LeaveStatus
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.LeaveRepository
import kz.alfasoft.mycompany.data.repo.SessionRepository
import kz.alfasoft.mycompany.domain.LeaveLedger
import java.time.LocalDate

data class TeamLeave(val employee: Employee, val until: LocalDate)

data class HomeUiState(
    val user: Employee? = null,
    val today: LocalDate? = null,
    val balance: LeaveBalance = LeaveBalance(0, 0, 0),
    val nextLeave: LeaveRequest? = null,
    val nextLeaveApprover: Employee? = null,
    val teamOnLeave: List<TeamLeave> = emptyList(),
    val pendingApprovals: Int = 0,
    /** У сотрудника без руководителя (гендиректор) заявок на отпуск нет. */
    val canRequestLeave: Boolean = false,
)

class HomeViewModel(
    employeeRepository: EmployeeRepository,
    leaveRepository: LeaveRepository,
    sessionRepository: SessionRepository,
    clock: AppClock,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        sessionRepository.currentUserId,
        employeeRepository.employees,
        leaveRepository.requests,
    ) { userId, employees, requests ->
        val user = employees.firstOrNull { it.id == userId } ?: return@combine HomeUiState()
        val today = clock.today()
        val byId = employees.associateBy { it.id }

        val balance = LeaveLedger.build(user, leaveRepository.carryOver[user.id] ?: 0, requests, today).balance

        val nextLeave = requests
            .filter { it.employeeId == user.id && !it.end.isBefore(today) }
            .filter { it.status == LeaveStatus.PENDING || it.status == LeaveStatus.APPROVED }
            .minByOrNull { it.start }

        val team = employees.filter { other ->
            other.id != user.id && (other.managerId == user.id ||
                (user.managerId != null && other.managerId == user.managerId))
        }.associateBy { it.id }
        val teamOnLeave = requests
            .filter { it.status == LeaveStatus.APPROVED && it.employeeId in team }
            .filter { !it.start.isAfter(today) && !it.end.isBefore(today) }
            .map { TeamLeave(team.getValue(it.employeeId), it.end) }
            .sortedBy { it.employee.fullName }

        HomeUiState(
            user = user,
            today = today,
            balance = balance,
            nextLeave = nextLeave,
            nextLeaveApprover = nextLeave?.let { byId[it.approverId] },
            teamOnLeave = teamOnLeave,
            pendingApprovals = requests.count { it.approverId == user.id && it.status == LeaveStatus.PENDING },
            canRequestLeave = user.managerId != null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

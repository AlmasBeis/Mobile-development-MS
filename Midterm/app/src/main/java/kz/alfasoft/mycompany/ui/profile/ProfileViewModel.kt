package kz.alfasoft.mycompany.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kz.alfasoft.mycompany.core.AppClock
import kz.alfasoft.mycompany.data.model.Department
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.data.model.LeaveBalance
import kz.alfasoft.mycompany.data.model.Office
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.LeaveRepository
import kz.alfasoft.mycompany.data.repo.SessionRepository
import kz.alfasoft.mycompany.domain.LeaveLedger
import kz.alfasoft.mycompany.ui.components.formatTenure

data class ProfileUiState(
    val employee: Employee? = null,
    val manager: Employee? = null,
    val office: Office? = null,
    val department: Department? = null,
    val subordinates: List<Employee> = emptyList(),
    val tenure: String = "",
    /** Только в личном кабинете. */
    val balance: LeaveBalance? = null,
    val loaded: Boolean = false,
)

internal fun buildProfileState(
    employeeId: String?,
    employees: List<Employee>,
    employeeRepository: EmployeeRepository,
    leaveRepository: LeaveRepository,
    requests: List<kz.alfasoft.mycompany.data.model.LeaveRequest>,
    clock: AppClock,
    withBalance: Boolean,
): ProfileUiState {
    val employee = employees.firstOrNull { it.id == employeeId } ?: return ProfileUiState(loaded = true)
    val today = clock.today()
    return ProfileUiState(
        employee = employee,
        manager = employees.firstOrNull { it.id == employee.managerId },
        office = employeeRepository.offices.firstOrNull { it.id == employee.officeId },
        department = employeeRepository.departments.firstOrNull { it.id == employee.departmentId },
        subordinates = employees.filter { it.managerId == employee.id }.sortedBy { it.fullName },
        tenure = formatTenure(employee.hireDate, today),
        balance = if (withBalance) {
            LeaveLedger.build(employee, leaveRepository.carryOver[employee.id] ?: 0, requests, today).balance
        } else null,
        loaded = true,
    )
}

class ProfileViewModel(
    private val employeeRepository: EmployeeRepository,
    private val leaveRepository: LeaveRepository,
    private val sessionRepository: SessionRepository,
    private val clock: AppClock,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        sessionRepository.currentUserId,
        employeeRepository.employees,
        leaveRepository.requests,
    ) { userId, employees, requests ->
        buildProfileState(userId, employees, employeeRepository, leaveRepository, requests, clock, withBalance = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())
}

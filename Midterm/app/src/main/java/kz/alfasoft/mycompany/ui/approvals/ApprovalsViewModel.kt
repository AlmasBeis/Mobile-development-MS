package kz.alfasoft.mycompany.ui.approvals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kz.alfasoft.mycompany.core.AppClock
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.model.LeaveStatus
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.LeaveRepository
import kz.alfasoft.mycompany.data.repo.SessionRepository
import kz.alfasoft.mycompany.domain.LeaveLedger

data class ApprovalItem(
    val request: LeaveRequest,
    val employee: Employee,
    val substitute: Employee?,
    val balanceAfter: Int,
    val conflicts: List<Employee>,
)

data class ApprovalsUiState(
    val loaded: Boolean = false,
    val pending: List<ApprovalItem> = emptyList(),
    val history: List<ApprovalItem> = emptyList(),
    val processingId: Int? = null,
)

class ApprovalsViewModel(
    employeeRepository: EmployeeRepository,
    private val leaveRepository: LeaveRepository,
    sessionRepository: SessionRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val processingId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<ApprovalsUiState> = combine(
        sessionRepository.currentUserId,
        employeeRepository.employees,
        leaveRepository.requests,
        processingId,
    ) { userId, employees, requests, processing ->
        val me = employees.firstOrNull { it.id == userId } ?: return@combine ApprovalsUiState()
        val byId = employees.associateBy { it.id }
        val myReports = employees.filter { it.managerId == me.id }.map { it.id }.toSet()
        val approvedByReports = requests.filter { it.status == LeaveStatus.APPROVED && it.employeeId in myReports }

        fun item(request: LeaveRequest): ApprovalItem? {
            val employee = byId[request.employeeId] ?: return null
            val balance = LeaveLedger.build(
                employee, leaveRepository.carryOver[employee.id] ?: 0, requests, clock.today(),
            ).balance
            return ApprovalItem(
                request = request,
                employee = employee,
                substitute = request.substituteId?.let(byId::get),
                balanceAfter = balance.available,
                conflicts = approvedByReports
                    .filter { it.employeeId != request.employeeId }
                    .filter { !request.end.isBefore(it.start) && !request.start.isAfter(it.end) }
                    .mapNotNull { byId[it.employeeId] }
                    .distinct(),
            )
        }

        val mine = requests.filter { it.approverId == me.id }
        ApprovalsUiState(
            loaded = true,
            pending = mine.filter { it.status == LeaveStatus.PENDING }
                .sortedWith(compareBy<LeaveRequest> { it.createdAt }.thenBy { it.id })
                .mapNotNull(::item),
            history = mine.filter { it.status != LeaveStatus.PENDING }
                .sortedWith(compareByDescending<LeaveRequest> { it.decidedAt ?: it.createdAt }.thenByDescending { it.id })
                .mapNotNull(::item),
            processingId = processing,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ApprovalsUiState())

    fun approve(requestId: Int) = process(requestId) { leaveRepository.approve(requestId, clock.today()) }

    fun reject(requestId: Int, reason: String) {
        if (reason.isBlank()) return
        process(requestId) { leaveRepository.reject(requestId, reason, clock.today()) }
    }

    private fun process(requestId: Int, action: suspend () -> Unit) {
        if (processingId.value != null) return
        processingId.value = requestId
        viewModelScope.launch {
            try {
                action()
            } finally {
                processingId.value = null
            }
        }
    }
}

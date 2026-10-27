package kz.alfasoft.mycompany.ui.leave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.alfasoft.mycompany.core.AppClock
import kz.alfasoft.mycompany.data.model.LeaveBalance
import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.model.LedgerEntry
import kz.alfasoft.mycompany.data.model.LedgerType
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.LeaveRepository
import kz.alfasoft.mycompany.data.repo.SessionRepository
import kz.alfasoft.mycompany.domain.LeaveLedger

enum class LedgerFilter(val label: String) {
    ALL("Все"), ACCRUALS("Начисления"), USAGE("Списания");

    fun matches(entry: LedgerEntry) = when (this) {
        ALL -> true
        ACCRUALS -> entry.type != LedgerType.USAGE
        USAGE -> entry.type == LedgerType.USAGE
    }
}

data class LeaveUiState(
    val loaded: Boolean = false,
    val balance: LeaveBalance = LeaveBalance(0, 0, 0),
    val requests: List<LeaveRequest> = emptyList(),
    /** Новые операции сверху, уже с учётом фильтра. */
    val ledger: List<LedgerEntry> = emptyList(),
    val filter: LedgerFilter = LedgerFilter.ALL,
    /** У сотрудника без руководителя (гендиректор) заявок на отпуск нет. */
    val canRequestLeave: Boolean = false,
    val cancellingId: Int? = null,
)

class LeaveViewModel(
    employeeRepository: EmployeeRepository,
    private val leaveRepository: LeaveRepository,
    sessionRepository: SessionRepository,
    clock: AppClock,
) : ViewModel() {

    private val filter = MutableStateFlow(LedgerFilter.ALL)
    private val cancellingId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<LeaveUiState> = combine(
        sessionRepository.currentUserId,
        employeeRepository.employees,
        leaveRepository.requests,
        filter,
        cancellingId,
    ) { userId, employees, requests, filter, cancelling ->
        val user = employees.firstOrNull { it.id == userId } ?: return@combine LeaveUiState()
        val result = LeaveLedger.build(user, leaveRepository.carryOver[user.id] ?: 0, requests, clock.today())
        LeaveUiState(
            loaded = true,
            balance = result.balance,
            requests = requests.filter { it.employeeId == user.id }
                .sortedWith(compareByDescending<LeaveRequest> { it.createdAt }.thenByDescending { it.id }),
            ledger = result.entries.filter(filter::matches).asReversed(),
            filter = filter,
            canRequestLeave = user.managerId != null,
            cancellingId = cancelling,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeaveUiState())

    fun onFilterSelected(value: LedgerFilter) = filter.update { value }

    fun cancel(requestId: Int) {
        if (cancellingId.value != null) return
        cancellingId.value = requestId
        viewModelScope.launch {
            try {
                leaveRepository.cancel(requestId)
            } finally {
                cancellingId.value = null
            }
        }
    }
}

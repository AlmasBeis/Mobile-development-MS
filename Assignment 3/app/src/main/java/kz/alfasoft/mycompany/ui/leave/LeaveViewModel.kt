package kz.alfasoft.mycompany.ui.leave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
    val ledger: List<LedgerEntry> = emptyList(),
    val filter: LedgerFilter = LedgerFilter.ALL,
    val canRequestLeave: Boolean = false,
    val cancellingId: Int? = null,
    val deletingId: Int? = null,
) {
    /** Пока идёт любой запрос по заявкам, кнопки на карточках неактивны. */
    val busy: Boolean get() = cancellingId != null || deletingId != null
}

class LeaveViewModel(
    employeeRepository: EmployeeRepository,
    private val leaveRepository: LeaveRepository,
    private val sessionRepository: SessionRepository,
    clock: AppClock,
) : ViewModel() {

    private val eventChannel = Channel<String>(Channel.BUFFERED)
    /** Сообщения для Snackbar. */
    val messages = eventChannel.receiveAsFlow()
    private val deletingId = MutableStateFlow<Int?>(null)

    private val filter = MutableStateFlow(LedgerFilter.ALL)
    private val cancellingId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<LeaveUiState> = combine(
        sessionRepository.currentUserId,
        employeeRepository.employees,
        leaveRepository.requests,
        filter,
        combine(cancellingId, deletingId) { c, d -> c to d },
    ) { userId, employees, requests, filter, (cancelling, deleting) ->
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
            deletingId = deleting,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeaveUiState())

    fun onFilterSelected(value: LedgerFilter) = filter.update { value }

    fun delete(requestId: Int) {
        val userId = sessionRepository.currentUserId.value ?: return
        if (cancellingId.value != null || deletingId.value != null) return
        deletingId.value = requestId
        viewModelScope.launch {
            try {
                val deleted = leaveRepository.delete(requestId, userId)
                eventChannel.send(if (deleted) "Заявка №$requestId удалена" else "Не удалось удалить заявку №$requestId")
            } finally {
                deletingId.value = null
            }
        }
    }

    fun cancel(requestId: Int) {
        val userId = sessionRepository.currentUserId.value ?: return
        if (cancellingId.value != null || deletingId.value != null) return
        cancellingId.value = requestId
        viewModelScope.launch {
            try {
                val cancelled = leaveRepository.cancel(requestId, userId)
                eventChannel.send(if (cancelled) "Заявка №$requestId отозвана" else "Не удалось отозвать заявку №$requestId")
            } finally {
                cancellingId.value = null
            }
        }
    }
}

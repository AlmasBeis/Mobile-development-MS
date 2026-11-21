package kz.alfasoft.mycompany.ui.leavedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kz.alfasoft.mycompany.core.AppClock
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.data.model.LeaveEvent
import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.LeaveRepository
import kz.alfasoft.mycompany.data.repo.SessionRepository
import kz.alfasoft.mycompany.domain.LeaveAction
import kz.alfasoft.mycompany.domain.LeaveFormInput
import kz.alfasoft.mycompany.domain.LeaveLedger
import kz.alfasoft.mycompany.domain.LeaveRequestValidator
import kz.alfasoft.mycompany.domain.LeaveWorkflow
import kz.alfasoft.mycompany.domain.ValidationContext
import kz.alfasoft.mycompany.ui.navigation.LeaveDetail

/** Событие истории с именем того, кто его сделал. */
data class HistoryItem(val event: LeaveEvent, val actor: Employee?)

sealed interface LeaveDetailUiState {
    data object Loading : LeaveDetailUiState

    /** Заявки нет в базе: например, её удалили, пока экран был в стеке. */
    data object NotFound : LeaveDetailUiState

    data class Content(
        val request: LeaveRequest,
        val employee: Employee?,
        val approver: Employee?,
        val substitute: Employee?,
        /** Остаток сотрудника, если заявку согласуют; показываем только согласующему. */
        val balanceAfter: Int?,
        /** Что текущий пользователь может сделать с заявкой — по правилам [LeaveWorkflow]. */
        val actions: Set<LeaveAction>,
        /** Почему черновик пока нельзя отправить (нет дней, пересечение и т. п.). */
        val submitBlockedReason: String?,
        val history: List<HistoryItem>,
        val processing: Boolean,
    ) : LeaveDetailUiState {
        fun can(action: LeaveAction) = action in actions
    }
}

sealed interface LeaveDetailEvent {
    data object Deleted : LeaveDetailEvent
    /** Отклонённая заявка вернулась в черновик — сразу открываем её редактирование. */
    data object ReturnedToDraft : LeaveDetailEvent
    data class Message(val text: String) : LeaveDetailEvent
}

/** Детальный экран заявки: id приходит из маршрута [LeaveDetail], данные — из Room через Flow. */
class LeaveDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val employeeRepository: EmployeeRepository,
    private val leaveRepository: LeaveRepository,
    private val sessionRepository: SessionRepository,
    private val clock: AppClock,
) : ViewModel() {
    val requestId: Int = savedStateHandle.toRoute<LeaveDetail>().id

    private val processing = MutableStateFlow(false)
    /** После удаления строка пропадает из базы; в это время показываем загрузку, а не «не найдена». */
    private val deleting = MutableStateFlow(false)
    private val eventChannel = Channel<LeaveDetailEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    val uiState: StateFlow<LeaveDetailUiState> = combine(
        combine(leaveRepository.observeRequest(requestId), leaveRepository.observeHistory(requestId)) { r, h -> r to h },
        leaveRepository.requests,
        employeeRepository.employees,
        sessionRepository.currentUserId,
        combine(processing, deleting) { p, d -> p to d },
    ) { (request, history), allRequests, employees, userId, (processing, deleting) ->
        if (request == null) {
            return@combine if (deleting) LeaveDetailUiState.Loading else LeaveDetailUiState.NotFound
        }
        val byId = employees.associateBy { it.id }
        val employee = byId[request.employeeId]
        val actions = LeaveWorkflow.allowedActions(request, userId)
        LeaveDetailUiState.Content(
            request = request,
            employee = employee,
            approver = byId[request.approverId],
            substitute = request.substituteId?.let(byId::get),
            balanceAfter = if (LeaveAction.APPROVE in actions && employee != null) {
                LeaveLedger.build(employee, leaveRepository.carryOver[employee.id] ?: 0, allRequests, clock.today())
                    .balance.available
            } else null,
            actions = actions,
            submitBlockedReason = if (LeaveAction.SUBMIT in actions && employee != null) {
                submitErrors(request, employee, allRequests).values.firstOrNull()
            } else null,
            history = history.map { HistoryItem(it, byId[it.actorId]) },
            processing = processing,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeaveDetailUiState.Loading)

    fun submit() = perform { userId ->
        // Проверяем по свежим данным: баланс мог измениться, пока экран был открыт.
        val request = leaveRepository.observeRequest(requestId).first() ?: return@perform false
        val employee = employeeRepository.employees.value.firstOrNull { it.id == userId } ?: return@perform false
        val errors = submitErrors(request, employee, leaveRepository.requests.first())
        if (errors.isNotEmpty()) {
            eventChannel.send(LeaveDetailEvent.Message(errors.values.first()))
            return@perform null
        }
        leaveRepository.submit(requestId, userId).also { ok ->
            if (ok) eventChannel.send(LeaveDetailEvent.Message("Заявка отправлена на согласование"))
        }
    }

    fun cancel() = perform { userId ->
        leaveRepository.cancel(requestId, userId).also { ok ->
            if (ok) eventChannel.send(LeaveDetailEvent.Message("Заявка отозвана"))
        }
    }

    fun approve() = perform { userId ->
        leaveRepository.approve(requestId, userId).also { ok ->
            if (ok) eventChannel.send(LeaveDetailEvent.Message("Заявка согласована"))
        }
    }

    fun reject(reason: String) {
        if (reason.isBlank()) return
        perform { userId ->
            leaveRepository.reject(requestId, userId, reason).also { ok ->
                if (ok) eventChannel.send(LeaveDetailEvent.Message("Заявка отклонена"))
            }
        }
    }

    fun returnToDraft() = perform { userId ->
        leaveRepository.returnToDraft(requestId, userId).also { ok ->
            if (ok) eventChannel.send(LeaveDetailEvent.ReturnedToDraft)
        }
    }

    fun delete() {
        deleting.value = true
        perform { userId ->
            leaveRepository.delete(requestId, userId).also { ok ->
                if (ok) eventChannel.send(LeaveDetailEvent.Deleted) else deleting.value = false
            }
        }
    }

    /**
     * Выполняет действие. [block] возвращает true — успех, false — статус успел измениться
     * (покажем ошибку), null — действие отменено с уже показанным сообщением.
     */
    private fun perform(block: suspend (userId: String) -> Boolean?) {
        val userId = sessionRepository.currentUserId.value
        if (userId == null || processing.value) {
            deleting.value = false
            return
        }
        processing.value = true
        viewModelScope.launch {
            try {
                if (block(userId) == false) {
                    eventChannel.send(LeaveDetailEvent.Message("Не удалось: статус заявки уже изменился"))
                }
            } finally {
                processing.value = false
            }
        }
    }

    /** Полная проверка черновика перед отправкой; сам черновик в баланс и пересечения не входит. */
    private fun submitErrors(request: LeaveRequest, employee: Employee, allRequests: List<LeaveRequest>) =
        allRequests.filter { it.id != request.id }.let { others ->
            val balance = LeaveLedger.build(employee, leaveRepository.carryOver[employee.id] ?: 0, others, clock.today()).balance
            LeaveRequestValidator.validate(
                LeaveFormInput(request.type, request.start, request.end, request.comment),
                ValidationContext(clock.today(), balance.available, others.filter { it.employeeId == employee.id }),
            )
        }
}

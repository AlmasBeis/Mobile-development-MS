package kz.alfasoft.mycompany.ui.leaveform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.alfasoft.mycompany.core.AppClock
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.model.LeaveStatus
import kz.alfasoft.mycompany.data.model.LeaveType
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.LeaveDraft
import kz.alfasoft.mycompany.data.repo.LeaveRepository
import kz.alfasoft.mycompany.data.repo.SessionRepository
import kz.alfasoft.mycompany.domain.FormField
import kz.alfasoft.mycompany.domain.LeaveAction
import kz.alfasoft.mycompany.domain.LeaveWorkflow
import kz.alfasoft.mycompany.domain.LeaveFormInput
import kz.alfasoft.mycompany.domain.LeaveLedger
import kz.alfasoft.mycompany.domain.LeaveRequestValidator
import kz.alfasoft.mycompany.domain.ValidationContext
import java.time.LocalDate

data class LeaveFormState(
    val type: LeaveType = LeaveType.ANNUAL,
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    val substituteId: String? = null,
    val comment: String = "",
)

data class LeaveFormUiState(
    val loaded: Boolean = false,
    val canRequestLeave: Boolean = false,
    /** id заявки, если форма открыта для редактирования; null — новая заявка. */
    val editingId: Int? = null,
    /** Почему заявку нельзя редактировать (уже согласована, удалена, чужая). */
    val editBlockedReason: String? = null,
    val form: LeaveFormState = LeaveFormState(),
    val today: LocalDate? = null,
    val available: Int = 0,
    val days: Int? = null,
    /** Только ошибки, которые уже пора показывать пользователю. */
    val errors: Map<FormField, String> = emptyMap(),
    /** Можно ли отправить на согласование (полная проверка: баланс, пересечения, причина). */
    val isValid: Boolean = false,
    /** Форма умеет сохранять черновик: новая заявка или редактирование черновика. */
    val canSaveDraft: Boolean = false,
    /** Для черновика достаточно корректных дат. */
    val isDraftValid: Boolean = false,
    val colleagues: List<Employee> = emptyList(),
    val approver: Employee? = null,
    val submitting: Boolean = false,
) {
    val isEditing: Boolean get() = editingId != null
}

sealed interface LeaveFormEvent {
    /** Заявка сохранена; форму можно закрывать и показать [message]. */
    data class Done(val message: String) : LeaveFormEvent
    data class Failed(val message: String) : LeaveFormEvent
}

/**
 * Одна форма на два маршрута: LeaveNew (создание) и LeaveEdit(id) (редактирование).
 * id заявки берём из SavedStateHandle: у LeaveNew его нет.
 */
class LeaveFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val employeeRepository: EmployeeRepository,
    private val leaveRepository: LeaveRepository,
    private val sessionRepository: SessionRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val editingId: Int? = savedStateHandle.get<Int>("id")

    private val form = MutableStateFlow(LeaveFormState())
    private val touched = MutableStateFlow<Set<FormField>>(emptySet())
    private val submitting = MutableStateFlow(false)
    /** Для новой заявки готово сразу; для редактирования — после заполнения полей из базы. */
    private val prefilled = MutableStateFlow(editingId == null)
    private val eventChannel = Channel<LeaveFormEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        if (editingId != null) {
            viewModelScope.launch {
                leaveRepository.observeRequest(editingId).first()?.let { request ->
                    form.value = LeaveFormState(
                        type = request.type,
                        start = request.start,
                        end = request.end,
                        substituteId = request.substituteId,
                        comment = request.comment,
                    )
                    // Поля уже заполнены — ошибки показываем сразу.
                    touched.value = FormField.entries.toSet()
                }
                prefilled.value = true
            }
        }
    }

    val uiState: StateFlow<LeaveFormUiState> = combine(
        combine(form, touched) { f, t -> f to t },
        combine(submitting, prefilled) { s, p -> s to p },
        sessionRepository.currentUserId,
        employeeRepository.employees,
        leaveRepository.requests,
    ) { (form, touched), (submitting, prefilled), userId, employees, requests ->
        val user = employees.firstOrNull { it.id == userId } ?: return@combine LeaveFormUiState()
        if (!prefilled) return@combine LeaveFormUiState(editingId = editingId)
        val context = validationContext(user, requests)
        val allErrors = validate(form, context)
        val start = form.start
        val end = form.end
        val bothDates = FormField.START in touched && FormField.END in touched
        val original = requests.firstOrNull { it.id == editingId }
        LeaveFormUiState(
            loaded = true,
            canRequestLeave = user.managerId != null,
            editingId = editingId,
            // Не блокируем во время сохранения: иначе мелькнёт сообщение до закрытия формы.
            editBlockedReason = if (submitting) null else editBlockedReason(user, requests),
            form = form,
            today = clock.today(),
            available = context.available,
            days = if (start != null && end != null && !end.isBefore(start)) {
                LeaveRequestValidator.daysBetween(start, end)
            } else null,
            // После отправки новая заявка сама пересекается с формой: ошибки уже не нужны.
            errors = if (submitting) emptyMap() else allErrors.filterKeys { field ->
                when (field) {
                    FormField.START, FormField.END -> field in touched
                    FormField.PERIOD -> bothDates
                    // «Укажите причину» подсказываем, как только выбраны обе даты: кнопка уже неактивна.
                    FormField.COMMENT -> field in touched || (form.type == LeaveType.UNPAID && bothDates)
                }
            },
            isValid = allErrors.isEmpty(),
            canSaveDraft = editingId == null || original?.status == LeaveStatus.DRAFT,
            isDraftValid = LeaveRequestValidator.validateDraft(form.toInput(), clock.today()).isEmpty(),
            colleagues = employees
                .filter { it.departmentId == user.departmentId && it.id != user.id }
                .sortedBy { it.fullName },
            approver = employees.firstOrNull { it.id == user.managerId },
            submitting = submitting,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeaveFormUiState(editingId = editingId))

    fun onTypeChange(type: LeaveType) = form.update { it.copy(type = type) }

    fun onStartChange(date: LocalDate) {
        form.update { it.copy(start = date) }
        touched.update { it + FormField.START }
    }

    fun onEndChange(date: LocalDate) {
        form.update { it.copy(end = date) }
        touched.update { it + FormField.END }
    }

    fun onSubstituteChange(id: String?) = form.update { it.copy(substituteId = id) }

    fun onCommentChange(text: String) {
        form.update { it.copy(comment = text) }
        touched.update { it + FormField.COMMENT }
    }

    /**
     * [send] = true — отправить на согласование (или сохранить изменения ожидающей заявки),
     * false — сохранить черновик без проверки баланса и пересечений.
     */
    fun submit(send: Boolean = true) {
        if (submitting.value || !prefilled.value) return
        // Данные могли измениться, пока форма была открыта, поэтому проверяем заново.
        val user = employeeRepository.employees.value.firstOrNull { it.id == sessionRepository.currentUserId.value } ?: return
        val approverId = user.managerId ?: return
        val current = form.value
        val start = current.start
        val end = current.end
        submitting.value = true
        viewModelScope.launch {
            try {
                // Берём свежий список из базы, а не из состояния экрана.
                val requests = leaveRepository.requests.first()
                val blocked = editBlockedReason(user, requests)
                if (blocked != null) {
                    submitting.value = false
                    eventChannel.send(LeaveFormEvent.Failed(blocked))
                    return@launch
                }
                val errors = if (send) {
                    validate(current, validationContext(user, requests))
                } else {
                    LeaveRequestValidator.validateDraft(current.toInput(), clock.today())
                }
                if (errors.isNotEmpty() || start == null || end == null) {
                    touched.value = FormField.entries.toSet()
                    submitting.value = false
                    return@launch
                }
                val draft = LeaveDraft(current.type, start, end, current.substituteId, current.comment)
                val wasDraft = editingId == null || requests.firstOrNull { it.id == editingId }?.status == LeaveStatus.DRAFT
                val saved = if (editingId == null) {
                    leaveRepository.create(user.id, approverId, draft, submit = send)
                    true
                } else {
                    leaveRepository.update(editingId, user.id, draft, submit = send)
                }
                // submitting остаётся true при успехе: форма закрывается, и её нельзя отправить повторно.
                if (saved) {
                    val approver = employeeRepository.employees.value.firstOrNull { it.id == approverId }
                    val message = when {
                        !send -> "Черновик сохранён"
                        wasDraft -> "Заявка отправлена на согласование: ${approver?.displayName.orEmpty()}"
                        else -> "Изменения сохранены"
                    }
                    eventChannel.send(LeaveFormEvent.Done(message))
                } else {
                    submitting.value = false
                    eventChannel.send(LeaveFormEvent.Failed("Не удалось сохранить: заявку уже нельзя изменить"))
                }
            } catch (e: Throwable) {
                submitting.value = false
                throw e
            }
        }
    }

    /** Редактировать можно только свой черновик или заявку «На согласовании» ([LeaveWorkflow]). */
    private fun editBlockedReason(user: Employee, requests: List<LeaveRequest>): String? {
        if (editingId == null) return null
        val request = requests.firstOrNull { it.id == editingId } ?: return "Заявка не найдена. Возможно, её удалили."
        return when {
            request.employeeId != user.id -> "Можно изменять только свои заявки"
            !LeaveWorkflow.canPerform(request, user.id, LeaveAction.EDIT) ->
                "Руководитель уже принял решение или заявка отозвана — изменить её нельзя"
            else -> null
        }
    }

    /**
     * При редактировании сама заявка не участвует в проверке: её дни возвращаются в доступный
     * остаток, а пересечение с собой не считается ошибкой.
     */
    private fun validationContext(user: Employee, requests: List<LeaveRequest>): ValidationContext {
        val today = clock.today()
        val others = requests.filter { it.id != editingId }
        val balance = LeaveLedger.build(user, leaveRepository.carryOver[user.id] ?: 0, others, today).balance
        return ValidationContext(today, balance.available, others.filter { it.employeeId == user.id })
    }

    private fun validate(form: LeaveFormState, context: ValidationContext) =
        LeaveRequestValidator.validate(form.toInput(), context)

    private fun LeaveFormState.toInput() = LeaveFormInput(type, start, end, comment)
}

package kz.alfasoft.mycompany.ui.leaveform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.alfasoft.mycompany.core.AppClock
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.model.LeaveType
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.LeaveRepository
import kz.alfasoft.mycompany.data.repo.SessionRepository
import kz.alfasoft.mycompany.domain.FormField
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
    val form: LeaveFormState = LeaveFormState(),
    val today: LocalDate? = null,
    val available: Int = 0,
    val days: Int? = null,
    /** Только ошибки, которые уже пора показывать пользователю. */
    val errors: Map<FormField, String> = emptyMap(),
    val isValid: Boolean = false,
    val colleagues: List<Employee> = emptyList(),
    val approver: Employee? = null,
    val submitting: Boolean = false,
)

sealed interface LeaveFormEvent {
    data class Submitted(val approverName: String) : LeaveFormEvent
}

class LeaveFormViewModel(
    private val employeeRepository: EmployeeRepository,
    private val leaveRepository: LeaveRepository,
    private val sessionRepository: SessionRepository,
    private val clock: AppClock,
) : ViewModel() {

    private val form = MutableStateFlow(LeaveFormState())
    private val touched = MutableStateFlow<Set<FormField>>(emptySet())
    private val submitting = MutableStateFlow(false)
    private val eventChannel = Channel<LeaveFormEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    val uiState: StateFlow<LeaveFormUiState> = combine(
        form,
        touched,
        submitting,
        sessionRepository.currentUserId,
        combine(employeeRepository.employees, leaveRepository.requests) { e, r -> e to r },
    ) { form, touched, submitting, userId, (employees, requests) ->
        val user = employees.firstOrNull { it.id == userId } ?: return@combine LeaveFormUiState()
        val context = validationContext(user, requests)
        val allErrors = validate(form, context)
        val start = form.start
        val end = form.end
        val bothDates = FormField.START in touched && FormField.END in touched
        LeaveFormUiState(
            loaded = true,
            canRequestLeave = user.managerId != null,
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
            colleagues = employees
                .filter { it.departmentId == user.departmentId && it.id != user.id }
                .sortedBy { it.fullName },
            approver = employees.firstOrNull { it.id == user.managerId },
            submitting = submitting,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeaveFormUiState())

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

    fun submit() {
        if (submitting.value) return
        // Данные могли измениться, пока форма была открыта, поэтому проверяем заново.
        val user = employeeRepository.employees.value.firstOrNull { it.id == sessionRepository.currentUserId.value } ?: return
        val approverId = user.managerId ?: return
        val current = form.value
        val start = current.start
        val end = current.end
        if (validate(current, validationContext(user, leaveRepository.requests.value)).isNotEmpty() || start == null || end == null) {
            touched.value = FormField.entries.toSet()
            return
        }
        submitting.value = true
        viewModelScope.launch {
            try {
                leaveRepository.create(user.id, approverId, current.type, start, end, current.substituteId, current.comment, clock.today())
                val approver = employeeRepository.employees.value.firstOrNull { it.id == approverId }
                // submitting остаётся true: форма закрывается, и её нельзя отправить повторно.
                eventChannel.send(LeaveFormEvent.Submitted(approver?.displayName.orEmpty()))
            } catch (e: Throwable) {
                submitting.value = false
                throw e
            }
        }
    }

    private fun validationContext(user: Employee, requests: List<LeaveRequest>): ValidationContext {
        val today = clock.today()
        val balance = LeaveLedger.build(user, leaveRepository.carryOver[user.id] ?: 0, requests, today).balance
        return ValidationContext(today, balance.available, requests.filter { it.employeeId == user.id })
    }

    private fun validate(form: LeaveFormState, context: ValidationContext) =
        LeaveRequestValidator.validate(LeaveFormInput(form.type, form.start, form.end, form.comment), context)
}

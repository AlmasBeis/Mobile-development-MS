package kz.alfasoft.mycompany.domain

import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.model.LeaveStatus
import kz.alfasoft.mycompany.data.model.LeaveType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class FormField { START, END, PERIOD, COMMENT }

data class LeaveFormInput(
    val type: LeaveType,
    val start: LocalDate?,
    val end: LocalDate?,
    val comment: String,
)

data class ValidationContext(
    val today: LocalDate,
    val available: Int,
    val ownRequests: List<LeaveRequest>,
)

object LeaveRequestValidator {
    const val MAX_UNPAID_DAYS = 30
    const val MAX_COMMENT_LENGTH = 300

    fun daysBetween(start: LocalDate, end: LocalDate): Int = (ChronoUnit.DAYS.between(start, end) + 1).toInt()

    fun validate(input: LeaveFormInput, ctx: ValidationContext): Map<FormField, String> {
        val errors = linkedMapOf<FormField, String>()
        val start = input.start
        val end = input.end

        when {
            start == null -> errors[FormField.START] = "Выберите дату начала"
            start < ctx.today.plusDays(1) ->
                errors[FormField.START] = "Отпуск можно оформить начиная с завтрашнего дня"
        }
        when {
            end == null -> errors[FormField.END] = "Выберите дату окончания"
            start != null && end < start -> errors[FormField.END] = "Дата окончания раньше даты начала"
        }

        if (start != null && end != null && !end.isBefore(start)) {
            val days = daysBetween(start, end)
            val periodError = when {
                input.type == LeaveType.ANNUAL && days > ctx.available ->
                    "Недостаточно дней: доступно ${ctx.available}, запрошено $days"
                input.type == LeaveType.UNPAID && days > MAX_UNPAID_DAYS ->
                    "Отпуск без сохранения з/п — не больше $MAX_UNPAID_DAYS дней"
                else -> overlapping(start, end, ctx.ownRequests)?.let {
                    "Пересекается с заявкой ${shortDate(it.start)}–${shortDate(it.end)}"
                }
            }
            if (periodError != null) errors[FormField.PERIOD] = periodError
        }

        when {
            input.comment.length > MAX_COMMENT_LENGTH -> errors[FormField.COMMENT] = "Не больше $MAX_COMMENT_LENGTH символов"
            input.type == LeaveType.UNPAID && input.comment.isBlank() -> errors[FormField.COMMENT] = "Укажите причину"
        }
        return errors
    }

    private fun overlapping(start: LocalDate, end: LocalDate, requests: List<LeaveRequest>): LeaveRequest? =
        requests
            .filter { it.status == LeaveStatus.PENDING || it.status == LeaveStatus.APPROVED }
            .firstOrNull { !end.isBefore(it.start) && !start.isAfter(it.end) }

    private fun shortDate(date: LocalDate) = "%02d.%02d".format(date.dayOfMonth, date.monthValue)
}

package kz.alfasoft.mycompany.domain

import kz.alfasoft.mycompany.data.model.LeaveEventType
import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.model.LeaveStatus

/** Действия, которые пользователь может выполнить с заявкой. */
enum class LeaveAction { EDIT, SUBMIT, CANCEL, APPROVE, REJECT, RETURN_TO_DRAFT, DELETE }

/**
 * Процесс согласования заявки:
 *
 * ```
 * DRAFT ──submit──▶ PENDING ──approve──▶ APPROVED
 *   ▲                  │ ├────reject───▶ REJECTED ──return──▶ DRAFT
 *   │                  │ └────cancel───▶ CANCELLED
 * ```
 * Автор редактирует черновик и ожидающую заявку, отправляет, отзывает и удаляет черновики,
 * отклонённые и отозванные. Решение принимает только согласующий. Согласованная заявка
 * окончательна.
 *
 * Проверка баланса и пересечений при отправке — в [LeaveRequestValidator].
 */
object LeaveWorkflow {

    fun allowedActions(request: LeaveRequest, userId: String?): Set<LeaveAction> {
        if (userId == null) return emptySet()
        val isAuthor = userId == request.employeeId
        val isApprover = userId == request.approverId
        return buildSet {
            when (request.status) {
                LeaveStatus.DRAFT -> if (isAuthor) addAll(listOf(LeaveAction.EDIT, LeaveAction.SUBMIT, LeaveAction.DELETE))
                LeaveStatus.PENDING -> {
                    if (isAuthor) addAll(listOf(LeaveAction.EDIT, LeaveAction.CANCEL))
                    if (isApprover) addAll(listOf(LeaveAction.APPROVE, LeaveAction.REJECT))
                }
                LeaveStatus.REJECTED -> if (isAuthor) addAll(listOf(LeaveAction.RETURN_TO_DRAFT, LeaveAction.DELETE))
                LeaveStatus.CANCELLED -> if (isAuthor) add(LeaveAction.DELETE)
                LeaveStatus.APPROVED -> Unit
            }
        }
    }

    fun canPerform(request: LeaveRequest, userId: String?, action: LeaveAction): Boolean =
        action in allowedActions(request, userId)

    /** Статус после действия; null — действие удаляет заявку. */
    fun nextStatus(current: LeaveStatus, action: LeaveAction): LeaveStatus? = when (action) {
        LeaveAction.EDIT -> current
        LeaveAction.SUBMIT -> LeaveStatus.PENDING
        LeaveAction.CANCEL -> LeaveStatus.CANCELLED
        LeaveAction.APPROVE -> LeaveStatus.APPROVED
        LeaveAction.REJECT -> LeaveStatus.REJECTED
        LeaveAction.RETURN_TO_DRAFT -> LeaveStatus.DRAFT
        LeaveAction.DELETE -> null
    }

    /** Какое событие пишем в историю; у удаления истории нет — она удаляется вместе с заявкой. */
    fun eventFor(action: LeaveAction): LeaveEventType? = when (action) {
        LeaveAction.EDIT -> LeaveEventType.EDITED
        LeaveAction.SUBMIT -> LeaveEventType.SUBMITTED
        LeaveAction.CANCEL -> LeaveEventType.CANCELLED
        LeaveAction.APPROVE -> LeaveEventType.APPROVED
        LeaveAction.REJECT -> LeaveEventType.REJECTED
        LeaveAction.RETURN_TO_DRAFT -> LeaveEventType.RETURNED_TO_DRAFT
        LeaveAction.DELETE -> null
    }
}

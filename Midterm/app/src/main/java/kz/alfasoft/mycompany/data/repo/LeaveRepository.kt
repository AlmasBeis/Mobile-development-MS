package kz.alfasoft.mycompany.data.repo

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kz.alfasoft.mycompany.data.mock.MockData
import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.model.LeaveStatus
import kz.alfasoft.mycompany.data.model.LeaveType
import java.time.LocalDate

class LeaveRepository {
    private val _requests = MutableStateFlow(MockData.leaveRequests)
    val requests: StateFlow<List<LeaveRequest>> = _requests.asStateFlow()

    val carryOver: Map<String, Int> = MockData.carryOver

    suspend fun create(
        employeeId: String,
        approverId: String,
        type: LeaveType,
        start: LocalDate,
        end: LocalDate,
        substituteId: String?,
        comment: String,
        today: LocalDate,
    ): LeaveRequest {
        delay(NETWORK_DELAY_MS) // имитация сети
        var created: LeaveRequest? = null
        _requests.update { list ->
            val request = LeaveRequest(
                id = (list.maxOfOrNull { it.id } ?: 0) + 1,
                employeeId = employeeId,
                approverId = approverId,
                type = type,
                start = start,
                end = end,
                substituteId = substituteId,
                comment = comment.trim(),
                status = LeaveStatus.PENDING,
                createdAt = today,
            )
            created = request
            list + request
        }
        return created!!
    }

    /** Согласование руководителем. Возвращает true, если заявка была «На согласовании». */
    suspend fun approve(requestId: Int, today: LocalDate): Boolean =
        decide(requestId, LeaveStatus.APPROVED, null, today)

    /** Отклонение руководителем; причина обязательна. */
    suspend fun reject(requestId: Int, reason: String, today: LocalDate): Boolean {
        require(reason.isNotBlank()) { "Причина отклонения обязательна" }
        return decide(requestId, LeaveStatus.REJECTED, reason.trim(), today)
    }

    private suspend fun decide(requestId: Int, status: LeaveStatus, comment: String?, today: LocalDate): Boolean {
        delay(NETWORK_DELAY_MS) // имитация сети
        var changed = false
        _requests.update { list ->
            list.map { request ->
                if (request.id == requestId && request.status == LeaveStatus.PENDING) {
                    changed = true
                    request.copy(status = status, decidedAt = today, decisionComment = comment)
                } else request
            }
        }
        return changed
    }

    /** Отзывает заявку, пока она ещё «На согласовании». Возвращает true, если статус изменился. */
    suspend fun cancel(requestId: Int): Boolean {
        delay(NETWORK_DELAY_MS) // имитация сети
        var changed = false
        _requests.update { list ->
            list.map { request ->
                if (request.id == requestId && request.status == LeaveStatus.PENDING) {
                    changed = true
                    request.copy(status = LeaveStatus.CANCELLED)
                } else request
            }
        }
        return changed
    }

    private companion object {
        const val NETWORK_DELAY_MS = 300L
    }
}

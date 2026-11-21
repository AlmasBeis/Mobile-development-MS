package kz.alfasoft.mycompany.data.repo

import androidx.room.withTransaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kz.alfasoft.mycompany.core.AppClock
import kz.alfasoft.mycompany.data.local.AppDatabase
import kz.alfasoft.mycompany.data.local.entity.LeaveEventEntity
import kz.alfasoft.mycompany.data.local.entity.LeaveRequestEntity
import kz.alfasoft.mycompany.data.local.toModel
import kz.alfasoft.mycompany.data.mock.MockData
import kz.alfasoft.mycompany.data.model.LeaveEvent
import kz.alfasoft.mycompany.data.model.LeaveEventType
import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.model.LeaveStatus
import kz.alfasoft.mycompany.data.model.LeaveType
import kz.alfasoft.mycompany.domain.LeaveAction
import kz.alfasoft.mycompany.domain.LeaveWorkflow
import java.time.LocalDate

data class LeaveDraft(
    val type: LeaveType,
    val start: LocalDate,
    val end: LocalDate,
    val substituteId: String?,
    val comment: String,
)
class LeaveRepository(
    private val database: AppDatabase,
    private val clock: AppClock,
) {
    private val requestDao = database.leaveRequestDao()
    private val eventDao = database.leaveEventDao()

    val requests: Flow<List<LeaveRequest>> = requestDao.observeAll().map { list -> list.map { it.toModel() } }

    /** Перенос остатка с прошлого года — справочные данные, в базе не хранятся. */
    val carryOver: Map<String, Int> = MockData.carryOver

    fun observeRequest(id: Int): Flow<LeaveRequest?> = requestDao.observeById(id).map { it?.toModel() }

    fun observeHistory(requestId: Int): Flow<List<LeaveEvent>> =
        eventDao.observeForRequest(requestId).map { list -> list.map { it.toModel() } }

    fun observeLatestEventFor(employeeId: String): Flow<LeaveEvent?> =
        eventDao.observeLatestForEmployee(employeeId).map { it?.toModel() }

    /** Создаёт заявку: черновиком или сразу «На согласовании» ([submit]). Возвращает её id. */
    suspend fun create(employeeId: String, approverId: String, draft: LeaveDraft, submit: Boolean): Int {
        delay(NETWORK_DELAY_MS) // имитация сети
        return database.withTransaction {
            val id = requestDao.insert(
                LeaveRequestEntity(
                    employeeId = employeeId,
                    approverId = approverId,
                    type = draft.type,
                    start = draft.start,
                    end = draft.end,
                    substituteId = draft.substituteId,
                    comment = draft.comment.trim(),
                    status = if (submit) LeaveStatus.PENDING else LeaveStatus.DRAFT,
                    createdAt = clock.today(),
                    decidedAt = null,
                    decisionComment = null,
                ),
            ).toInt()
            log(id, LeaveEventType.CREATED, employeeId)
            if (submit) log(id, LeaveEventType.SUBMITTED, employeeId)
            id
        }
    }
    suspend fun update(requestId: Int, employeeId: String, draft: LeaveDraft, submit: Boolean): Boolean {
        delay(NETWORK_DELAY_MS) // имитация сети
        return database.withTransaction {
            val current = requestDao.getById(requestId) ?: return@withTransaction false
            if (!LeaveWorkflow.canPerform(current.toModel(), employeeId, LeaveAction.EDIT)) return@withTransaction false
            val sendNow = submit && current.status == LeaveStatus.DRAFT
            requestDao.update(
                current.copy(
                    type = draft.type,
                    start = draft.start,
                    end = draft.end,
                    substituteId = draft.substituteId,
                    comment = draft.comment.trim(),
                    status = if (sendNow) LeaveStatus.PENDING else current.status,
                ),
            )
            log(requestId, LeaveEventType.EDITED, employeeId)
            if (sendNow) log(requestId, LeaveEventType.SUBMITTED, employeeId)
            true
        }
    }

    suspend fun submit(requestId: Int, employeeId: String): Boolean =
        transition(requestId, employeeId, LeaveAction.SUBMIT)

    suspend fun cancel(requestId: Int, employeeId: String): Boolean =
        transition(requestId, employeeId, LeaveAction.CANCEL)

    /** «Исправить и отправить заново»: отклонённая заявка снова становится черновиком. */
    suspend fun returnToDraft(requestId: Int, employeeId: String): Boolean =
        transition(requestId, employeeId, LeaveAction.RETURN_TO_DRAFT) { current ->
            // Прошлое решение остаётся в истории, а у черновика его быть не должно.
            current.copy(status = LeaveStatus.DRAFT, decidedAt = null, decisionComment = null) to
                LeaveEventType.RETURNED_TO_DRAFT
        }

    suspend fun approve(requestId: Int, approverId: String): Boolean =
        transition(requestId, approverId, LeaveAction.APPROVE) { current ->
            current.copy(status = LeaveStatus.APPROVED, decidedAt = clock.today()) to LeaveEventType.APPROVED
        }

    /** Отклонение руководителем; причина обязательна и попадает в историю. */
    suspend fun reject(requestId: Int, approverId: String, reason: String): Boolean {
        require(reason.isNotBlank()) { "Причина отклонения обязательна" }
        val trimmed = reason.trim()
        return transition(requestId, approverId, LeaveAction.REJECT, comment = trimmed) { current ->
            current.copy(status = LeaveStatus.REJECTED, decidedAt = clock.today(), decisionComment = trimmed) to
                LeaveEventType.REJECTED
        }
    }

    /** Удаляет заявку; история удаляется вместе с ней (ForeignKey CASCADE). */
    suspend fun delete(requestId: Int, employeeId: String): Boolean {
        delay(NETWORK_DELAY_MS) // имитация сети
        return database.withTransaction {
            val current = requestDao.getById(requestId) ?: return@withTransaction false
            if (!LeaveWorkflow.canPerform(current.toModel(), employeeId, LeaveAction.DELETE)) return@withTransaction false
            requestDao.delete(current) > 0
        }
    }
    private suspend fun transition(
        requestId: Int,
        actorId: String,
        action: LeaveAction,
        comment: String? = null,
        change: (LeaveRequestEntity) -> Pair<LeaveRequestEntity, LeaveEventType> = { current ->
            current.copy(status = LeaveWorkflow.nextStatus(current.status, action)!!) to
                LeaveWorkflow.eventFor(action)!!
        },
    ): Boolean {
        delay(NETWORK_DELAY_MS) // имитация сети
        return database.withTransaction {
            val current = requestDao.getById(requestId) ?: return@withTransaction false
            if (!LeaveWorkflow.canPerform(current.toModel(), actorId, action)) return@withTransaction false
            val (updated, event) = change(current)
            requestDao.update(updated)
            log(requestId, event, actorId, comment)
            true
        }
    }

    private suspend fun log(requestId: Int, type: LeaveEventType, actorId: String, comment: String? = null) {
        eventDao.insert(LeaveEventEntity(requestId = requestId, type = type, actorId = actorId, at = clock.now(), comment = comment))
    }

    private companion object {
        const val NETWORK_DELAY_MS = 300L
    }
}

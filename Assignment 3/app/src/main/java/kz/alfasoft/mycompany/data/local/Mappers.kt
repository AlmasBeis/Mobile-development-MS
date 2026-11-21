package kz.alfasoft.mycompany.data.local

import kz.alfasoft.mycompany.data.local.entity.LeaveEventEntity
import kz.alfasoft.mycompany.data.local.entity.LeaveRequestEntity
import kz.alfasoft.mycompany.data.model.LeaveEvent
import kz.alfasoft.mycompany.data.model.LeaveRequest

fun LeaveRequestEntity.toModel() = LeaveRequest(
    id = id,
    employeeId = employeeId,
    approverId = approverId,
    type = type,
    start = start,
    end = end,
    substituteId = substituteId,
    comment = comment,
    status = status,
    createdAt = createdAt,
    decidedAt = decidedAt,
    decisionComment = decisionComment,
)

fun LeaveRequest.toEntity() = LeaveRequestEntity(
    id = id,
    employeeId = employeeId,
    approverId = approverId,
    type = type,
    start = start,
    end = end,
    substituteId = substituteId,
    comment = comment,
    status = status,
    createdAt = createdAt,
    decidedAt = decidedAt,
    decisionComment = decisionComment,
)

fun LeaveEventEntity.toModel() = LeaveEvent(
    id = id,
    requestId = requestId,
    type = type,
    actorId = actorId,
    at = at,
    comment = comment,
)

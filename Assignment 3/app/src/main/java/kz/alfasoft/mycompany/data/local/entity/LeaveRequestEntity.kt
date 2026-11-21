package kz.alfasoft.mycompany.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kz.alfasoft.mycompany.data.model.LeaveStatus
import kz.alfasoft.mycompany.data.model.LeaveType
import java.time.LocalDate

@Entity(
    tableName = "leave_requests",
    indices = [Index("employee_id"), Index("approver_id")],
)
data class LeaveRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "employee_id") val employeeId: String,
    @ColumnInfo(name = "approver_id") val approverId: String,
    val type: LeaveType,
    @ColumnInfo(name = "start_date") val start: LocalDate,
    @ColumnInfo(name = "end_date") val end: LocalDate,
    @ColumnInfo(name = "substitute_id") val substituteId: String?,
    val comment: String,
    val status: LeaveStatus,
    @ColumnInfo(name = "created_at") val createdAt: LocalDate,
    @ColumnInfo(name = "decided_at") val decidedAt: LocalDate?,
    @ColumnInfo(name = "decision_comment") val decisionComment: String?,
)

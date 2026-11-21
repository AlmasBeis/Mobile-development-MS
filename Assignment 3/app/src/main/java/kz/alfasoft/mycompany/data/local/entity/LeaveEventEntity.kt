package kz.alfasoft.mycompany.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kz.alfasoft.mycompany.data.model.LeaveEventType
import java.time.LocalDateTime

@Entity(
    tableName = "leave_events",
    foreignKeys = [
        ForeignKey(
            entity = LeaveRequestEntity::class,
            parentColumns = ["id"],
            childColumns = ["request_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("request_id")],
)
data class LeaveEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "request_id") val requestId: Int,
    val type: LeaveEventType,
    @ColumnInfo(name = "actor_id") val actorId: String,
    val at: LocalDateTime,
    val comment: String?,
)

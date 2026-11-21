package kz.alfasoft.mycompany.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kz.alfasoft.mycompany.data.local.entity.LeaveEventEntity

@Dao
interface LeaveEventDao {
    /** История одной заявки, от старых событий к новым. */
    @Query("SELECT * FROM leave_events WHERE request_id = :requestId ORDER BY at, id")
    fun observeForRequest(requestId: Int): Flow<List<LeaveEventEntity>>

    /** Последнее событие по заявкам сотрудника, кем бы оно ни было сделано (для главной). */
    @Query(
        """
        SELECT e.* FROM leave_events e
        JOIN leave_requests r ON r.id = e.request_id
        WHERE r.employee_id = :employeeId
        ORDER BY e.at DESC, e.id DESC
        LIMIT 1
        """
    )
    fun observeLatestForEmployee(employeeId: String): Flow<LeaveEventEntity?>

    @Insert
    suspend fun insert(event: LeaveEventEntity): Long
}

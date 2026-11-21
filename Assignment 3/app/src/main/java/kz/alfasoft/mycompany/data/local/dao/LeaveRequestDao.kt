package kz.alfasoft.mycompany.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kz.alfasoft.mycompany.data.local.entity.LeaveRequestEntity

@Dao
interface LeaveRequestDao {
    /** Все заявки; Flow заново выдаёт список при любом изменении таблицы. */
    @Query("SELECT * FROM leave_requests ORDER BY id")
    fun observeAll(): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests WHERE id = :id")
    fun observeById(id: Int): Flow<LeaveRequestEntity?>

    @Query("SELECT * FROM leave_requests WHERE id = :id")
    suspend fun getById(id: Int): LeaveRequestEntity?

    /** Возвращает id новой строки. */
    @Insert
    suspend fun insert(request: LeaveRequestEntity): Long

    /** Возвращает число изменённых строк. */
    @Update
    suspend fun update(request: LeaveRequestEntity): Int

    @Delete
    suspend fun delete(request: LeaveRequestEntity): Int
}

package kz.alfasoft.mycompany.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kz.alfasoft.mycompany.data.local.dao.LeaveEventDao
import kz.alfasoft.mycompany.data.local.dao.LeaveRequestDao
import kz.alfasoft.mycompany.data.local.entity.LeaveEventEntity
import kz.alfasoft.mycompany.data.local.entity.LeaveRequestEntity
import kz.alfasoft.mycompany.data.mock.MockData

@Database(
    entities = [LeaveRequestEntity::class, LeaveEventEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun leaveRequestDao(): LeaveRequestDao
    abstract fun leaveEventDao(): LeaveEventDao

    companion object {
        private const val NAME = "mycompany.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NAME)
                .addCallback(object : Callback() {
                    // Демо-данные пишутся только при первом создании файла базы, в той же транзакции,
                    // поэтому изменения пользователя (в том числе удаления) после перезапуска не затираются.
                    override fun onCreate(db: SupportSQLiteDatabase) = seed(db)
                })
                .build()

        private fun seed(db: SupportSQLiteDatabase) {
            MockData.leaveRequests.forEach { r ->
                db.execSQL(
                    "INSERT INTO leave_requests (id, employee_id, approver_id, type, start_date, end_date, " +
                        "substitute_id, comment, status, created_at, decided_at, decision_comment) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any?>(
                        r.id, r.employeeId, r.approverId, r.type.name, r.start.toString(), r.end.toString(),
                        r.substituteId, r.comment, r.status.name, r.createdAt.toString(),
                        r.decidedAt?.toString(), r.decisionComment,
                    ),
                )
            }
            seedHistory(db)
        }

        private fun seedHistory(db: SupportSQLiteDatabase) {
            val insert = "INSERT INTO leave_events (request_id, type, actor_id, at, comment) "
            db.execSQL(insert + "SELECT id, 'CREATED', employee_id, created_at || 'T09:00', NULL FROM leave_requests")
            db.execSQL(
                insert + "SELECT id, 'SUBMITTED', employee_id, created_at || 'T09:01', NULL FROM leave_requests " +
                    "WHERE status <> 'DRAFT'",
            )
            db.execSQL(
                insert + "SELECT id, status, approver_id, COALESCE(decided_at, created_at) || 'T10:00', " +
                    "decision_comment FROM leave_requests WHERE status IN ('APPROVED', 'REJECTED')",
            )
            db.execSQL(
                insert + "SELECT id, 'CANCELLED', employee_id, created_at || 'T12:00', NULL FROM leave_requests " +
                    "WHERE status = 'CANCELLED'",
            )
        }
    }
}

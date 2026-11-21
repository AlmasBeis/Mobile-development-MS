package kz.alfasoft.mycompany

import android.content.Context
import kz.alfasoft.mycompany.core.AppClock
import kz.alfasoft.mycompany.data.local.AppDatabase
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.LeaveRepository
import kz.alfasoft.mycompany.data.repo.SessionRepository

/** Ручной DI: одна база и один набор репозиториев на процесс. */
class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.build(context)

    val clock = AppClock()
    val employeeRepository = EmployeeRepository()
    val leaveRepository = LeaveRepository(database, clock)
    val sessionRepository = SessionRepository()
}

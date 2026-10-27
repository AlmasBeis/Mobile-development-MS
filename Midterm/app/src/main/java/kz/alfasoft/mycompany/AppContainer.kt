package kz.alfasoft.mycompany

import kz.alfasoft.mycompany.core.AppClock
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.LeaveRepository
import kz.alfasoft.mycompany.data.repo.SessionRepository

class AppContainer {
    val clock = AppClock()
    val employeeRepository = EmployeeRepository()
    val leaveRepository = LeaveRepository()
    val sessionRepository = SessionRepository()
}

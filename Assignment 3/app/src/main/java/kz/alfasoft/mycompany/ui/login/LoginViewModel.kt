package kz.alfasoft.mycompany.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kz.alfasoft.mycompany.data.mock.MockData
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.SessionRepository

class LoginViewModel(
    employeeRepository: EmployeeRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    val demoUsers: StateFlow<List<Employee>> = employeeRepository.employees
        .map { employees -> MockData.demoUserIds.mapNotNull { id -> employees.firstOrNull { it.id == id } } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun login(employeeId: String) = sessionRepository.login(employeeId)
}

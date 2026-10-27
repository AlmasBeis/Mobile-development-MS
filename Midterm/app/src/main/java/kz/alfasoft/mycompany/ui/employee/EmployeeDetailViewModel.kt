package kz.alfasoft.mycompany.ui.employee

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kz.alfasoft.mycompany.core.AppClock
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.LeaveRepository
import kz.alfasoft.mycompany.ui.navigation.Employee
import kz.alfasoft.mycompany.ui.profile.ProfileUiState
import kz.alfasoft.mycompany.ui.profile.buildProfileState

/** Карточка чужого сотрудника: id приходит из маршрута [Employee]. */
class EmployeeDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val employeeRepository: EmployeeRepository,
    private val leaveRepository: LeaveRepository,
    private val clock: AppClock,
) : ViewModel() {
    private val employeeId = savedStateHandle.toRoute<Employee>().id

    val uiState: StateFlow<ProfileUiState> = combine(
        employeeRepository.employees,
        leaveRepository.requests,
    ) { employees, requests ->
        buildProfileState(employeeId, employees, employeeRepository, leaveRepository, requests, clock, withBalance = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())
}

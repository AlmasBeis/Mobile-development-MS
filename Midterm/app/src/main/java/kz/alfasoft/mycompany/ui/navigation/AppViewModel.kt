package kz.alfasoft.mycompany.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.SessionRepository

data class AppUiState(
    val userId: String? = null,
    val isManager: Boolean = false,
)

class AppViewModel(
    employeeRepository: EmployeeRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    val uiState: StateFlow<AppUiState> = combine(
        sessionRepository.currentUserId,
        employeeRepository.employees,
    ) { userId, employees ->
        AppUiState(
            userId = userId,
            isManager = userId != null && employees.any { it.managerId == userId },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    fun logout() = sessionRepository.logout()
}

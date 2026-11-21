package kz.alfasoft.mycompany.data.repo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionRepository {
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    fun login(employeeId: String) {
        _currentUserId.value = employeeId
    }

    fun logout() {
        _currentUserId.value = null
    }
}

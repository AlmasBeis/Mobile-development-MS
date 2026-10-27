package kz.alfasoft.mycompany.ui.org

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kz.alfasoft.mycompany.core.AppClock
import kz.alfasoft.mycompany.data.model.Department
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.data.model.LeaveStatus
import kz.alfasoft.mycompany.data.repo.EmployeeRepository
import kz.alfasoft.mycompany.data.repo.LeaveRepository
import kz.alfasoft.mycompany.data.repo.SessionRepository
import kz.alfasoft.mycompany.domain.OrgTree
import java.time.LocalDate

data class OrgUiState(
    val currentUserId: String? = null,
    val allEmployees: List<Employee> = emptyList(),
    val groups: List<Pair<Char, List<Employee>>> = emptyList(),
    val foundCount: Int = 0,
    val departmentChips: List<Department> = emptyList(),
    val onLeaveUntil: Map<String, LocalDate> = emptyMap(),
    val defaultExpanded: Set<String> = emptySet(),
    val allExpandable: Set<String> = emptySet(),
)

@OptIn(FlowPreview::class)
class OrgViewModel(
    private val savedStateHandle: SavedStateHandle,
    employeeRepository: EmployeeRepository,
    leaveRepository: LeaveRepository,
    sessionRepository: SessionRepository,
    clock: AppClock,
) : ViewModel() {

    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")
    val departmentId: StateFlow<String?> = savedStateHandle.getStateFlow<String?>(KEY_DEPARTMENT, null)

    private val departments = employeeRepository.departments
    private val chips = departments.filter { it.parentId == null || departments.any { root -> root.parentId == null && root.id == it.parentId } }

    val uiState: StateFlow<OrgUiState> = combine(
        query.debounce(SEARCH_DEBOUNCE_MS),
        departmentId,
        employeeRepository.employees,
        leaveRepository.requests,
        sessionRepository.currentUserId,
    ) { query, department, employees, requests, userId ->
        val today = clock.today()
        val found = OrgTree.search(employees, query, department, departments)
        OrgUiState(
            currentUserId = userId,
            allEmployees = employees,
            groups = OrgTree.groupByInitial(found),
            foundCount = found.size,
            departmentChips = chips,
            onLeaveUntil = requests
                .filter { it.status == LeaveStatus.APPROVED && !it.start.isAfter(today) && !it.end.isBefore(today) }
                .associate { it.employeeId to it.end },
            defaultExpanded = userId?.let { OrgTree.ancestors(it, employees).toSet() }.orEmpty(),
            allExpandable = OrgTree.nodesWithSubordinates(employees),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrgUiState())

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
    }

    fun onDepartmentSelected(id: String?) {
        savedStateHandle[KEY_DEPARTMENT] = id
    }

    private companion object {
        const val KEY_QUERY = "query"
        const val KEY_DEPARTMENT = "department"
        const val SEARCH_DEBOUNCE_MS = 200L
    }
}

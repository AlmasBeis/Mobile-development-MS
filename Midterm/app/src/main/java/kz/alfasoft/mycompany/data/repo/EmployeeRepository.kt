package kz.alfasoft.mycompany.data.repo

import kz.alfasoft.mycompany.data.mock.MockData
import kz.alfasoft.mycompany.data.model.Department
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.data.model.Office
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EmployeeRepository {
    private val _employees = MutableStateFlow(MockData.employees)
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    val offices: List<Office> = MockData.offices
    val departments: List<Department> = MockData.departments
}

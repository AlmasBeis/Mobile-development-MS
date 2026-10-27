package kz.alfasoft.mycompany.domain

import kz.alfasoft.mycompany.data.model.Department
import kz.alfasoft.mycompany.data.model.Employee

data class OrgRow(
    val employee: Employee,
    val depth: Int,
    val subordinatesCount: Int,
    val expanded: Boolean,
)

object OrgTree {
    private val byName = compareBy<Employee>({ it.lastName }, { it.firstName })

    fun childrenIndex(employees: List<Employee>): Map<String?, List<Employee>> {
        val ids = employees.map { it.id }.toSet()
        return employees
            .groupBy { if (it.managerId in ids) it.managerId else null }
            .mapValues { (_, list) -> list.sortedWith(byName) }
    }

    fun ancestors(employeeId: String, employees: List<Employee>): List<String> {
        val byId = employees.associateBy { it.id }
        val chain = ArrayDeque<String>()
        val visited = mutableSetOf(employeeId)
        var current = byId[employeeId]?.managerId
        while (current != null && byId.containsKey(current) && visited.add(current)) {
            chain.addFirst(current)
            current = byId.getValue(current).managerId
        }
        return chain.toList()
    }

    fun nodesWithSubordinates(employees: List<Employee>): Set<String> =
        employees.mapNotNull { it.managerId }.toSet() intersect employees.map { it.id }.toSet()

    fun flatten(employees: List<Employee>, expanded: Set<String>): List<OrgRow> {
        val index = childrenIndex(employees)
        val rows = mutableListOf<OrgRow>()
        fun walk(parentId: String?, depth: Int) {
            index[parentId].orEmpty().forEach { employee ->
                val count = index[employee.id].orEmpty().size
                val isExpanded = count > 0 && employee.id in expanded
                rows += OrgRow(employee, depth, count, isExpanded)
                if (isExpanded) walk(employee.id, depth + 1)
            }
        }
        walk(null, 0)
        return rows
    }


    fun departmentMatches(filterId: String, employeeDepartmentId: String, departments: List<Department>): Boolean {
        if (filterId == employeeDepartmentId) return true
        val byId = departments.associateBy { it.id }
        if (byId[filterId]?.parentId == null) return false
        var current = byId[employeeDepartmentId]?.parentId
        val visited = mutableSetOf<String>()
        while (current != null && visited.add(current)) {
            if (current == filterId) return true
            current = byId[current]?.parentId
        }
        return false
    }

    fun search(
        employees: List<Employee>,
        query: String,
        departmentId: String?,
        departments: List<Department>,
    ): List<Employee> {
        val needle = query.trim().lowercase()
        return employees
            .filter { departmentId == null || departmentMatches(departmentId, it.departmentId, departments) }
            .filter {
                needle.isEmpty() ||
                    it.fullName.lowercase().contains(needle) ||
                    it.position.lowercase().contains(needle) ||
                    it.email.lowercase().contains(needle)
            }
            .sortedWith(byName)
    }

    fun groupByInitial(employees: List<Employee>): List<Pair<Char, List<Employee>>> =
        employees.groupBy { it.lastName.first().uppercaseChar() }.toList()
}

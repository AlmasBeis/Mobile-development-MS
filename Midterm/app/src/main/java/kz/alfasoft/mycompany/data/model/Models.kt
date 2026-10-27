package kz.alfasoft.mycompany.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Office(val id: String, val city: String, val address: String)

data class Department(val id: String, val name: String, val parentId: String?)

data class LanguageSkill(val language: String, val level: String)

data class Employee(
    val id: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val position: String,
    val departmentId: String,
    val managerId: String?,
    val email: String,
    val phone: String,
    val officeId: String,
    val hireDate: LocalDate,
    val languages: List<LanguageSkill>,
) {
    val fullName: String get() = listOfNotNull(lastName, firstName, middleName).joinToString(" ")
    val displayName: String get() = "$firstName $lastName"
    val initials: String get() = "${lastName.first()}${firstName.first()}"
}

enum class LeaveType { ANNUAL, UNPAID }

enum class LeaveStatus { PENDING, APPROVED, REJECTED, CANCELLED }

data class LeaveRequest(
    val id: Int,
    val employeeId: String,
    val approverId: String,
    val type: LeaveType,
    val start: LocalDate,
    val end: LocalDate,
    val substituteId: String?,
    val comment: String,
    val status: LeaveStatus,
    val createdAt: LocalDate,
    val decidedAt: LocalDate? = null,
    val decisionComment: String? = null,
) {
    val days: Int get() = (ChronoUnit.DAYS.between(start, end) + 1).toInt()
}

enum class LedgerType { CARRY_OVER, ACCRUAL, USAGE }

data class LedgerEntry(
    val employeeId: String,
    val date: LocalDate,
    val type: LedgerType,
    val days: Int,
    val title: String,
    val requestId: Int?,
    val balanceAfter: Int = 0,
)

data class LeaveBalance(val accrued: Int, val used: Int, val reserved: Int) {
    val available: Int get() = accrued - used - reserved
}

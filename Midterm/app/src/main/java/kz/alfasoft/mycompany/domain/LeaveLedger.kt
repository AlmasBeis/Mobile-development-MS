package kz.alfasoft.mycompany.domain

import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.data.model.LeaveBalance
import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.model.LeaveStatus
import kz.alfasoft.mycompany.data.model.LeaveType
import kz.alfasoft.mycompany.data.model.LedgerEntry
import kz.alfasoft.mycompany.data.model.LedgerType
import java.time.LocalDate

data class LeaveLedgerResult(
    val entries: List<LedgerEntry>,
    val balance: LeaveBalance,
)

object LeaveLedger {
    const val ACCRUAL_PER_MONTH = 2

    private val monthNames = listOf(
        "январь", "февраль", "март", "апрель", "май", "июнь",
        "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь",
    )

    fun build(
        employee: Employee,
        carryOver: Int,
        requests: List<LeaveRequest>,
        today: LocalDate,
    ): LeaveLedgerResult {
        val year = today.year
        val entries = mutableListOf<LedgerEntry>()

        entries += LedgerEntry(
            employee.id, LocalDate.of(year, 1, 1), LedgerType.CARRY_OVER, carryOver,
            "Перенос остатка с ${year - 1} года", null,
        )

        val firstHireMonth = employee.hireDate.withDayOfMonth(1)
        for (month in 1..12) {
            val date = LocalDate.of(year, month, 1)
            if (date.isAfter(today) || date.isBefore(firstHireMonth)) continue
            entries += LedgerEntry(
                employee.id, date, LedgerType.ACCRUAL, ACCRUAL_PER_MONTH,
                "Начисление за ${monthNames[month - 1]}", null,
            )
        }

        val own = requests.filter { it.employeeId == employee.id && it.type == LeaveType.ANNUAL }
        val approved = own.filter { it.status == LeaveStatus.APPROVED }
        approved.forEach { request ->
            entries += LedgerEntry(
                employee.id, request.start, LedgerType.USAGE, -request.days,
                "Отпуск ${formatShort(request.start)}–${formatShort(request.end)} (заявка №${request.id})",
                request.id,
            )
        }

        var running = 0
        val sorted = entries
            .sortedWith(compareBy({ it.date }, { it.type.ordinal }))
            .map { entry ->
                running += entry.days
                entry.copy(balanceAfter = running)
            }

        val balance = LeaveBalance(
            accrued = carryOver + sorted.filter { it.type == LedgerType.ACCRUAL }.sumOf { it.days },
            used = approved.sumOf { it.days },
            reserved = own.filter { it.status == LeaveStatus.PENDING }.sumOf { it.days },
        )
        return LeaveLedgerResult(sorted, balance)
    }

    private fun formatShort(date: LocalDate) = "%02d.%02d".format(date.dayOfMonth, date.monthValue)
}

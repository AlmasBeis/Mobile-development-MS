package kz.alfasoft.mycompany.ui.components

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ru = Locale.forLanguageTag("ru")
private val dayMonth = DateTimeFormatter.ofPattern("d MMMM", ru)
private val dayMonthYear = DateTimeFormatter.ofPattern("d MMMM yyyy", ru)
private val weekdayDate = DateTimeFormatter.ofPattern("EEEE, d MMMM", ru)

fun pluralRu(n: Int, one: String, few: String, many: String): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when {
        mod100 in 11..14 -> many
        mod10 == 1 -> one
        mod10 in 2..4 -> few
        else -> many
    }
}

fun daysLabel(n: Int) = "$n ${pluralRu(n, "день", "дня", "дней")}"

fun requestsLabel(n: Int) = "$n ${pluralRu(n, "заявка", "заявки", "заявок")}"

/** «16–20 ноября 2026» или «26 октября – 6 ноября 2026». */
fun formatPeriod(start: LocalDate, end: LocalDate): String = when {
    start == end -> dayMonthYear.format(start)
    start.year == end.year && start.month == end.month ->
        "${start.dayOfMonth}–${dayMonthYear.format(end)}"
    start.year == end.year -> "${dayMonth.format(start)} – ${dayMonthYear.format(end)}"
    else -> "${dayMonthYear.format(start)} – ${dayMonthYear.format(end)}"
}

fun formatDayMonth(date: LocalDate): String = dayMonth.format(date)

fun formatWeekdayDate(date: LocalDate): String =
    weekdayDate.format(date).replaceFirstChar { it.uppercase(ru) }

/** «2 года 7 месяцев», «5 месяцев», «меньше месяца». */
fun formatTenure(from: LocalDate, to: LocalDate): String {
    val period = java.time.Period.between(from, to)
    val years = period.years
    val months = period.months
    val parts = buildList {
        if (years > 0) add("$years ${pluralRu(years, "год", "года", "лет")}")
        if (months > 0) add("$months ${pluralRu(months, "месяц", "месяца", "месяцев")}")
    }
    return if (parts.isEmpty()) "меньше месяца" else parts.joinToString(" ")
}

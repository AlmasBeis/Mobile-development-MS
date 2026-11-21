package kz.alfasoft.mycompany.core

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AppClock(private val fixedToday: LocalDate = DEMO_TODAY) {
    fun today(): LocalDate = fixedToday

    /** Дата — демо-«сегодня», время — настоящее: события в истории идут по порядку. */
    fun now(): LocalDateTime = LocalDateTime.of(fixedToday, LocalTime.now().withNano(0))

    companion object {
        val DEMO_TODAY: LocalDate = LocalDate.of(2026, 10, 23)
    }
}

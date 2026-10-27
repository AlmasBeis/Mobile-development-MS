package kz.alfasoft.mycompany.core

import java.time.LocalDate

class AppClock(private val fixedToday: LocalDate = DEMO_TODAY) {
    fun today(): LocalDate = fixedToday

    companion object {
        val DEMO_TODAY: LocalDate = LocalDate.of(2026, 10, 23)
    }
}

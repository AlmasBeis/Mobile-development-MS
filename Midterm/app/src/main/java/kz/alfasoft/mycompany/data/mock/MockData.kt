package kz.alfasoft.mycompany.data.mock

import kz.alfasoft.mycompany.data.model.Department
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.data.model.LanguageSkill
import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.model.LeaveStatus
import kz.alfasoft.mycompany.data.model.LeaveType
import kz.alfasoft.mycompany.data.model.Office
import java.time.LocalDate

object MockData {
    /** Пользователи на экране входа: сотрудник, руководитель, топ-менеджер. */
    val demoUserIds = listOf("E04", "E03", "E01")

    val offices = listOf(
        Office("ALA", "Алматы", "пр. Аль-Фараби, 77/7, 12 этаж"),
        Office("AST", "Астана", "пр. Мангилик Ел, 55/20, офис 304"),
    )

    val departments = listOf(
        Department("exec", "Руководство", null),
        Department("dev", "Разработка", "exec"),
        Department("mobile", "Мобильная команда", "dev"),
        Department("backend", "Backend", "dev"),
        Department("hr", "Персонал", "exec"),
        Department("fin", "Финансы", "exec"),
    )

    private fun langs(vararg pairs: Pair<String, String>) =
        pairs.map { (language, level) -> LanguageSkill(language, level) }

    private val kk = "Казахский"
    private val ru = "Русский"
    private val en = "Английский"

    val employees = listOf(
        Employee(
            "E01", "Мурат", "Абенов", "Серикович", "Генеральный директор", "exec", null,
            "m.abenov@alfasoft.kz", "+7 727 355 01 01", "ALA", LocalDate.of(2018, 2, 1),
            langs(kk to "родной", ru to "свободно", en to "C1"),
        ),
        Employee(
            "E02", "Тимур", "Ахметов", "Болатович", "Технический директор", "exec", "E01",
            "t.akhmetov@alfasoft.kz", "+7 727 355 01 02", "ALA", LocalDate.of(2018, 6, 4),
            langs(kk to "родной", ru to "свободно", en to "C1"),
        ),
        Employee(
            "E03", "Ерлан", "Нурланов", "Маратович", "Руководитель мобильной разработки", "mobile", "E02",
            "e.nurlanov@alfasoft.kz", "+7 727 355 01 03", "ALA", LocalDate.of(2019, 9, 2),
            langs(kk to "родной", ru to "свободно", en to "B2"),
        ),
        Employee(
            "E04", "Айгерим", "Сапарова", "Канатовна", "Android-разработчик", "mobile", "E03",
            "a.saparova@alfasoft.kz", "+7 727 355 01 04", "ALA", LocalDate.of(2024, 3, 15),
            langs(kk to "родной", ru to "свободно", en to "B2"),
        ),
        Employee(
            "E05", "Данияр", "Касымов", "Ерланович", "iOS-разработчик", "mobile", "E03",
            "d.kassymov@alfasoft.kz", "+7 7172 55 01 05", "AST", LocalDate.of(2022, 1, 10),
            langs(kk to "родной", ru to "свободно", en to "B1"),
        ),
        Employee(
            "E06", "Алия", "Жумабаева", "Нуржановна", "QA-инженер", "mobile", "E03",
            "a.zhumabayeva@alfasoft.kz", "+7 727 355 01 06", "ALA", LocalDate.of(2023, 5, 22),
            langs(kk to "родной", ru to "свободно", en to "B1"),
        ),
        Employee(
            "E07", "Дина", "Мухамедова", "Асхатовна", "UI/UX-дизайнер", "mobile", "E03",
            "d.mukhamedova@alfasoft.kz", "+7 727 355 01 07", "ALA", LocalDate.of(2021, 11, 8),
            langs(ru to "родной", en to "C1"),
        ),
        Employee(
            "E08", "Сергей", "Ким", "Викторович", "Руководитель backend-разработки", "backend", "E02",
            "s.kim@alfasoft.kz", "+7 7172 55 01 08", "AST", LocalDate.of(2019, 3, 18),
            langs(ru to "родной", kk to "базовый", en to "C1"),
        ),
        Employee(
            "E09", "Нурсултан", "Оспанов", "Бакытович", "Backend-разработчик", "backend", "E08",
            "n.ospanov@alfasoft.kz", "+7 7172 55 01 09", "AST", LocalDate.of(2023, 8, 7),
            langs(kk to "родной", ru to "свободно", en to "B2"),
        ),
        Employee(
            "E10", "Ольга", "Смирнова", "Игоревна", "DevOps-инженер", "backend", "E08",
            "o.smirnova@alfasoft.kz", "+7 7172 55 01 10", "AST", LocalDate.of(2021, 4, 12),
            langs(ru to "родной", en to "B2"),
        ),
        Employee(
            "E11", "Гульнара", "Тлеубаева", "Ахметовна", "HR-директор", "hr", "E01",
            "g.tleubayeva@alfasoft.kz", "+7 727 355 01 11", "ALA", LocalDate.of(2018, 9, 3),
            langs(kk to "родной", ru to "свободно", en to "B2"),
        ),
        Employee(
            "E12", "Мадина", "Ержанова", "Сериковна", "HR-менеджер", "hr", "E11",
            "m.yerzhanova@alfasoft.kz", "+7 727 355 01 12", "ALA", LocalDate.of(2022, 7, 25),
            langs(kk to "родной", ru to "свободно"),
        ),
        Employee(
            "E13", "Асель", "Бекова", "Муратовна", "Финансовый директор", "fin", "E01",
            "a.bekova@alfasoft.kz", "+7 727 355 01 13", "ALA", LocalDate.of(2019, 1, 14),
            langs(kk to "родной", ru to "свободно", en to "C1"),
        ),
        Employee(
            "E14", "Арман", "Искаков", "Даулетович", "Бухгалтер", "fin", "E13",
            "a.iskakov@alfasoft.kz", "+7 727 355 01 14", "ALA", LocalDate.of(2020, 10, 5),
            langs(kk to "родной", ru to "свободно"),
        ),
    )

    /** Остаток дней, перенесённый с 2025 года на 01.01.2026. */
    val carryOver = mapOf(
        "E01" to 0, "E02" to 4, "E03" to 8, "E04" to 6, "E05" to 3, "E06" to 5, "E07" to 2,
        "E08" to 7, "E09" to 1, "E10" to 0, "E11" to 4, "E12" to 2, "E13" to 6, "E14" to 3,
    )

    private fun d(month: Int, day: Int) = LocalDate.of(2026, month, day)

    val leaveRequests = listOf(
        LeaveRequest(
            1, "E04", "E03", LeaveType.ANNUAL, d(5, 4), d(5, 13), null, "",
            LeaveStatus.APPROVED, d(4, 10), d(4, 11),
        ),
        LeaveRequest(
            2, "E04", "E03", LeaveType.ANNUAL, d(11, 16), d(11, 20), "E07", "",
            LeaveStatus.PENDING, d(10, 20),
        ),
        LeaveRequest(
            3, "E06", "E03", LeaveType.ANNUAL, d(10, 26), d(11, 6), null, "",
            LeaveStatus.PENDING, d(10, 21),
        ),
        LeaveRequest(
            4, "E05", "E03", LeaveType.ANNUAL, d(10, 20), d(10, 30), null, "",
            LeaveStatus.APPROVED, d(9, 28), d(9, 29),
        ),
        LeaveRequest(
            5, "E09", "E08", LeaveType.ANNUAL, d(9, 10), d(9, 19), null, "",
            LeaveStatus.REJECTED, d(8, 25), d(8, 26), "Релиз 15.09, перенеси на октябрь",
        ),
        LeaveRequest(
            6, "E07", "E03", LeaveType.UNPAID, d(7, 2), d(7, 3), null, "Семейные обстоятельства",
            LeaveStatus.CANCELLED, d(6, 20),
        ),
    )
}

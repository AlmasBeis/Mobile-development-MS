package kz.alfasoft.mycompany.ui.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kz.alfasoft.mycompany.ui.components.EmployeeAvatar
import kz.alfasoft.mycompany.ui.components.EmployeeListItem
import kz.alfasoft.mycompany.ui.components.InfoRow
import kz.alfasoft.mycompany.ui.components.LanguageChips
import kz.alfasoft.mycompany.ui.components.SectionCard
import kz.alfasoft.mycompany.ui.components.daysLabel

@Composable
fun ProfileContent(
    state: ProfileUiState,
    onOpenEmployee: (String) -> Unit,
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit = {},
) {
    val employee = state.employee ?: return
    val context = LocalContext.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            EmployeeAvatar(employee, size = 80.dp)
            Text(employee.fullName, style = MaterialTheme.typography.headlineSmall)
            Text(employee.position, style = MaterialTheme.typography.bodyLarge)
            state.department?.let {
                Text(
                    it.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SectionCard(title = "Контакты") {
            InfoRow(
                label = "Рабочая почта",
                value = employee.email,
                valueColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    try {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${employee.email}")))
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, "Нет почтового приложения", Toast.LENGTH_SHORT).show()
                    }
                },
            )
            InfoRow(label = "Рабочий телефон", value = employee.phone)
        }

        SectionCard(title = "Место работы") {
            state.office?.let { InfoRow(label = "Адрес", value = "${it.city}, ${it.address}") }
            InfoRow(label = "Дата приёма", value = formatHireDate(employee.hireDate))
            InfoRow(label = "Стаж", value = state.tenure)
        }

        SectionCard(title = "Знание языков") {
            LanguageChips(employee.languages)
        }

        state.manager?.let { manager ->
            SectionCard(title = "Руководитель") {
                EmployeeListItem(manager, onClick = { onOpenEmployee(manager.id) })
            }
        }

        if (state.subordinates.isNotEmpty()) {
            SectionCard(title = "Подчинённые (${state.subordinates.size})") {
                state.subordinates.forEach { sub ->
                    EmployeeListItem(sub, onClick = { onOpenEmployee(sub.id) })
                }
            }
        }

        footer()
    }
}

private fun formatHireDate(date: java.time.LocalDate) =
    "%02d.%02d.%d".format(date.dayOfMonth, date.monthValue, date.year)

@Composable
fun VacationWidget(availableDays: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier.clickable(onClick = onClick), title = "Отпуск") {
        Text("Доступно: ${daysLabel(availableDays)}", style = MaterialTheme.typography.bodyLarge)
        Text(
            "Подробнее о балансе и заявках",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 0.dp),
        )
    }
}

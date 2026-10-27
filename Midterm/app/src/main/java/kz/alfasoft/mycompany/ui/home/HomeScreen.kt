package kz.alfasoft.mycompany.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.alfasoft.mycompany.ui.AppViewModelProvider
import kz.alfasoft.mycompany.ui.components.SectionCard
import kz.alfasoft.mycompany.ui.components.StatusChip
import kz.alfasoft.mycompany.ui.components.daysLabel
import kz.alfasoft.mycompany.ui.components.formatDayMonth
import kz.alfasoft.mycompany.ui.components.formatPeriod
import kz.alfasoft.mycompany.ui.components.formatWeekdayDate
import kz.alfasoft.mycompany.ui.components.requestsLabel

@Composable
fun HomeScreen(
    onRequestLeave: () -> Unit,
    onOpenApprovals: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(state, onRequestLeave, onOpenApprovals, modifier)
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onRequestLeave: () -> Unit,
    onOpenApprovals: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val user = state.user ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text("Добрый день, ${user.firstName}!", style = MaterialTheme.typography.headlineSmall)
            state.today?.let {
                Text(
                    formatWeekdayDate(it),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.pendingApprovals > 0) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Ждут вашего решения: ${requestsLabel(state.pendingApprovals)}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Button(onClick = onOpenApprovals) { Text("Перейти к согласованиям") }
                }
            }
        }

        SectionCard {
            Text(
                "Доступно для отпуска: ${daysLabel(state.balance.available)}",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                "Начислено ${state.balance.accrued} · использовано ${state.balance.used} · " +
                    "на согласовании ${state.balance.reserved}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.canRequestLeave) {
                Button(onClick = onRequestLeave) { Text("Оформить отпуск") }
            }
        }

        SectionCard(title = "Ближайший отпуск") {
            val next = state.nextLeave
            if (next != null) {
                Text(
                    "${formatPeriod(next.start, next.end)} · ${daysLabel(next.days)}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatusChip(next.status)
                    state.nextLeaveApprover?.let {
                        Text(
                            "Руководитель: ${it.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Text("Запланированных отпусков нет. Самое время отдохнуть 🌴", style = MaterialTheme.typography.bodyLarge)
                if (state.canRequestLeave) {
                    Button(onClick = onRequestLeave) { Text("Оформить отпуск") }
                }
            }
        }

        if (state.teamOnLeave.isNotEmpty()) {
            SectionCard(title = "Сейчас в отпуске в вашей команде") {
                state.teamOnLeave.forEach {
                    Text(
                        "${it.employee.displayName} — до ${formatDayMonth(it.until)}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

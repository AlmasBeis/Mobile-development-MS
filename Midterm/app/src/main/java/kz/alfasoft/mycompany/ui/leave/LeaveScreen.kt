package kz.alfasoft.mycompany.ui.leave

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.alfasoft.mycompany.ui.AppViewModelProvider
import kz.alfasoft.mycompany.ui.components.BalanceCard
import kz.alfasoft.mycompany.ui.components.LedgerRow
import kz.alfasoft.mycompany.ui.components.LeaveRequestCard

@Composable
fun LeaveScreen(
    onNewRequest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LeaveViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LeaveContent(
        state = state,
        onNewRequest = onNewRequest,
        onFilterSelected = viewModel::onFilterSelected,
        onCancelConfirmed = viewModel::cancel,
        modifier = modifier,
    )
}

@Composable
private fun LeaveContent(
    state: LeaveUiState,
    onNewRequest: () -> Unit,
    onFilterSelected: (LedgerFilter) -> Unit,
    onCancelConfirmed: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Какую заявку просят отозвать — чисто UI-состояние диалога, переживает поворот.
    var requestToCancel by rememberSaveable { mutableStateOf<Int?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { BalanceCard(state.balance) }

            if (!state.canRequestLeave && state.loaded) {
                item {
                    Text(
                        "Ваши отпуска оформляются через HR-отдел",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item { Text("Мои заявки", style = MaterialTheme.typography.titleMedium) }
            if (state.requests.isEmpty()) {
                item {
                    Text(
                        "Заявок пока нет",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(state.requests, key = { "request-${it.id}" }) { request ->
                LeaveRequestCard(
                    request = request,
                    onCancel = { requestToCancel = request.id },
                    cancelling = state.cancellingId == request.id,
                    cancelEnabled = state.cancellingId == null,
                )
            }

            item { Text("Журнал движения дней", style = MaterialTheme.typography.titleMedium) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LedgerFilter.entries.forEach { option ->
                        FilterChip(
                            selected = state.filter == option,
                            onClick = { onFilterSelected(option) },
                            label = { Text(option.label) },
                        )
                    }
                }
            }
            items(state.ledger, key = { "ledger-${it.date}-${it.type}-${it.title}" }) { entry ->
                LedgerRow(entry)
                HorizontalDivider()
            }
        }

        if (state.canRequestLeave) {
            ExtendedFloatingActionButton(
                onClick = onNewRequest,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Новая заявка") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }

    requestToCancel?.let { id ->
        AlertDialog(
            onDismissRequest = { requestToCancel = null },
            title = { Text("Отозвать заявку №$id?") },
            text = { Text("Заявка станет «Отозвано», зарезервированные дни вернутся в баланс.") },
            confirmButton = {
                TextButton(onClick = {
                    onCancelConfirmed(id)
                    requestToCancel = null
                }) { Text("Отозвать") }
            },
            dismissButton = { TextButton(onClick = { requestToCancel = null }) { Text("Отмена") } },
        )
    }
}

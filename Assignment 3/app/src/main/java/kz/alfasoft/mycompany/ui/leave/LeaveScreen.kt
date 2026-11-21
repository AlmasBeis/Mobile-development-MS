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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
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
import kz.alfasoft.mycompany.ui.components.ConfirmDialog
import kz.alfasoft.mycompany.ui.components.EmptyState
import kz.alfasoft.mycompany.ui.components.LedgerRow
import kz.alfasoft.mycompany.ui.components.LeaveRequestCard

@Composable
fun LeaveScreen(
    onNewRequest: () -> Unit,
    onOpenRequest: (Int) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LeaveViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnMessage by rememberUpdatedState(onMessage)
    LaunchedEffect(viewModel) { viewModel.messages.collect { currentOnMessage(it) } }
    LeaveContent(
        state = state,
        onNewRequest = onNewRequest,
        onOpenRequest = onOpenRequest,
        onFilterSelected = viewModel::onFilterSelected,
        onCancelConfirmed = viewModel::cancel,
        onDeleteConfirmed = viewModel::delete,
        modifier = modifier,
    )
}

@Composable
private fun LeaveContent(
    state: LeaveUiState,
    onNewRequest: () -> Unit,
    onOpenRequest: (Int) -> Unit,
    onFilterSelected: (LedgerFilter) -> Unit,
    onCancelConfirmed: (Int) -> Unit,
    onDeleteConfirmed: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Какую заявку просят отозвать — чисто UI-состояние диалога, переживает поворот.
    var requestToCancel by rememberSaveable { mutableStateOf<Int?>(null) }
    var requestToDelete by rememberSaveable { mutableStateOf<Int?>(null) }

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
            if (state.requests.isEmpty() && state.loaded) {
                item {
                    EmptyState(
                        icon = Icons.Default.DateRange,
                        title = "Заявок пока нет",
                        message = if (state.canRequestLeave) {
                            "Создайте первую заявку: можно сначала сохранить черновик и отправить позже."
                        } else {
                            "Здесь появятся ваши заявки на отпуск."
                        },
                        actionLabel = if (state.canRequestLeave) "Новая заявка" else null,
                        onAction = onNewRequest,
                    )
                }
            }
            items(state.requests, key = { "request-${it.id}" }) { request ->
                LeaveRequestCard(
                    request = request,
                    onCancel = { requestToCancel = request.id },
                    onClick = { onOpenRequest(request.id) },
                    cancelling = state.cancellingId == request.id,
                    cancelEnabled = !state.busy,
                    onDelete = { requestToDelete = request.id },
                    deleting = state.deletingId == request.id,
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
        ConfirmDialog(
            title = "Отозвать заявку №$id?",
            text = "Заявка станет «Отозвано», зарезервированные дни вернутся в баланс.",
            confirmLabel = "Отозвать",
            onConfirm = {
                onCancelConfirmed(id)
                requestToCancel = null
            },
            onDismiss = { requestToCancel = null },
        )
    }
    requestToDelete?.let { id ->
        ConfirmDialog(
            title = "Удалить заявку №$id?",
            text = "Заявка исчезнет из списка без возможности восстановления.",
            confirmLabel = "Удалить",
            destructive = true,
            onConfirm = {
                onDeleteConfirmed(id)
                requestToDelete = null
            },
            onDismiss = { requestToDelete = null },
        )
    }
}

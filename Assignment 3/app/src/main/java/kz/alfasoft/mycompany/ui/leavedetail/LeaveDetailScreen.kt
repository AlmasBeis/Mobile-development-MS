package kz.alfasoft.mycompany.ui.leavedetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.domain.LeaveAction
import kz.alfasoft.mycompany.ui.AppViewModelProvider
import kz.alfasoft.mycompany.ui.components.ConfirmDialog
import kz.alfasoft.mycompany.ui.components.EmployeeAvatar
import kz.alfasoft.mycompany.ui.components.EmptyState
import kz.alfasoft.mycompany.ui.components.HistoryTimeline
import kz.alfasoft.mycompany.ui.components.InfoRow
import kz.alfasoft.mycompany.ui.components.RejectDialog
import kz.alfasoft.mycompany.ui.components.SectionCard
import kz.alfasoft.mycompany.ui.components.StatusChip
import kz.alfasoft.mycompany.ui.components.daysLabel
import kz.alfasoft.mycompany.ui.components.formatDate
import kz.alfasoft.mycompany.ui.components.formatPeriod
import kz.alfasoft.mycompany.ui.components.leaveTypeLabel

/** Все действия детального экрана; передаются в stateless-часть одним объектом. */
data class LeaveDetailActions(
    val onEdit: () -> Unit,
    val onSubmit: () -> Unit,
    val onCancel: () -> Unit,
    val onApprove: () -> Unit,
    val onReject: (String) -> Unit,
    val onReturnToDraft: () -> Unit,
    val onDelete: () -> Unit,
)

@Composable
fun LeaveDetailScreen(
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onDeleted: () -> Unit,
    onMessage: (String) -> Unit,
    onOpenEmployee: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LeaveDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnDeleted by rememberUpdatedState(onDeleted)
    val currentOnMessage by rememberUpdatedState(onMessage)
    val currentOnEdit by rememberUpdatedState(onEdit)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LeaveDetailEvent.Deleted -> currentOnDeleted()
                LeaveDetailEvent.ReturnedToDraft -> {
                    currentOnMessage("Заявка снова черновик — исправьте и отправьте")
                    currentOnEdit(viewModel.requestId)
                }
                is LeaveDetailEvent.Message -> currentOnMessage(event.text)
            }
        }
    }
    LeaveDetailContent(
        requestId = viewModel.requestId,
        state = state,
        onBack = onBack,
        onOpenEmployee = onOpenEmployee,
        actions = LeaveDetailActions(
            onEdit = { onEdit(viewModel.requestId) },
            onSubmit = viewModel::submit,
            onCancel = viewModel::cancel,
            onApprove = viewModel::approve,
            onReject = viewModel::reject,
            onReturnToDraft = viewModel::returnToDraft,
            onDelete = viewModel::delete,
        ),
        modifier = modifier,
    )
}

private enum class PendingDialog { CANCEL, DELETE, REJECT }

@Composable
fun LeaveDetailContent(
    requestId: Int,
    state: LeaveDetailUiState,
    onBack: () -> Unit,
    onOpenEmployee: (String) -> Unit,
    actions: LeaveDetailActions,
    modifier: Modifier = Modifier,
) {
    // Открытый диалог и введённая причина — UI-состояние, переживают поворот.
    var dialog by rememberSaveable { mutableStateOf<PendingDialog?>(null) }
    var reason by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text("Заявка №$requestId", style = MaterialTheme.typography.titleMedium)
        }

        when (state) {
            LeaveDetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            LeaveDetailUiState.NotFound -> EmptyState(
                icon = Icons.Default.Info,
                title = "Заявка не найдена",
                message = "Возможно, её удалили на другом экране.",
                actionLabel = "Назад",
                onAction = onBack,
            )
            is LeaveDetailUiState.Content -> RequestDetails(
                state = state,
                onOpenEmployee = onOpenEmployee,
                actions = actions.copy(
                    onCancel = { dialog = PendingDialog.CANCEL },
                    onDelete = { dialog = PendingDialog.DELETE },
                    onReject = {
                        reason = ""
                        dialog = PendingDialog.REJECT
                    },
                ),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }

    // Пока шёл диалог, заявку мог обработать кто-то другой: тогда действие недоступно и диалог не показываем.
    val content = state as? LeaveDetailUiState.Content
    when (dialog) {
        PendingDialog.CANCEL -> if (content?.can(LeaveAction.CANCEL) == true) {
            ConfirmDialog(
                title = "Отозвать заявку №$requestId?",
                text = "Заявка станет «Отозвано», зарезервированные дни вернутся в баланс.",
                confirmLabel = "Отозвать",
                onConfirm = {
                    actions.onCancel()
                    dialog = null
                },
                onDismiss = { dialog = null },
            )
        }
        PendingDialog.DELETE -> if (content?.can(LeaveAction.DELETE) == true) {
            ConfirmDialog(
                title = "Удалить заявку №$requestId?",
                text = "Заявка и вся её история исчезнут без возможности восстановления.",
                confirmLabel = "Удалить",
                destructive = true,
                onConfirm = {
                    actions.onDelete()
                    dialog = null
                },
                onDismiss = { dialog = null },
            )
        }
        PendingDialog.REJECT -> if (content?.can(LeaveAction.REJECT) == true) {
            RejectDialog(
                employeeName = content.employee?.displayName ?: "№$requestId",
                reason = reason,
                onReasonChange = { reason = it },
                onConfirm = {
                    actions.onReject(reason)
                    dialog = null
                },
                onDismiss = { dialog = null },
            )
        }
        null -> Unit
    }
}

@Composable
private fun RequestDetails(
    state: LeaveDetailUiState.Content,
    onOpenEmployee: (String) -> Unit,
    actions: LeaveDetailActions,
    modifier: Modifier = Modifier,
) {
    val request = state.request
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    formatPeriod(request.start, request.end),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(request.status)
            }
            Text(
                "${leaveTypeLabel(request.type)} · ${daysLabel(request.days)}",
                style = MaterialTheme.typography.bodyLarge,
            )
            InfoRow("Создана", formatDate(request.createdAt))
            if (request.comment.isNotBlank()) InfoRow("Комментарий", request.comment)
        }

        ActionButtons(state, actions)

        SectionCard(title = "Участники") {
            PersonRow("Сотрудник", state.employee, onOpenEmployee)
            PersonRow("Согласующий", state.approver, onOpenEmployee)
            if (state.substitute != null) {
                PersonRow("Замещает", state.substitute, onOpenEmployee)
            } else {
                InfoRow("Замещает", "Не выбран")
            }
        }

        if (request.decidedAt != null || request.decisionComment != null) {
            SectionCard(title = "Решение") {
                request.decidedAt?.let { InfoRow("Дата решения", formatDate(it)) }
                request.decisionComment?.let { InfoRow("Комментарий руководителя", it) }
            }
        }

        SectionCard(title = "История") {
            if (state.history.isEmpty()) {
                Text(
                    "История пока пуста",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                HistoryTimeline(state.history.map { it.event to it.actor })
            }
        }
    }
}

/** Показываем только разрешённые [kz.alfasoft.mycompany.domain.LeaveWorkflow] действия. */
@Composable
private fun ActionButtons(state: LeaveDetailUiState.Content, actions: LeaveDetailActions) {
    if (state.actions.isEmpty()) return
    val enabled = !state.processing
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.balanceAfter?.let {
            Text(
                "Остаток сотрудника после согласования: ${daysLabel(it)}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (it < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
        }
        if (state.can(LeaveAction.SUBMIT)) {
            state.submitBlockedReason?.let {
                Text("Пока нельзя отправить: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (state.can(LeaveAction.SUBMIT)) {
                Button(onClick = actions.onSubmit, enabled = enabled && state.submitBlockedReason == null) {
                    Text(if (state.processing) "Подождите…" else "Отправить на согласование")
                }
            }
            if (state.can(LeaveAction.APPROVE)) {
                Button(onClick = actions.onApprove, enabled = enabled) {
                    Text(if (state.processing) "Подождите…" else "Согласовать")
                }
            }
            if (state.can(LeaveAction.REJECT)) {
                OutlinedButton(onClick = { actions.onReject("") }, enabled = enabled) { Text("Отклонить") }
            }
            if (state.can(LeaveAction.RETURN_TO_DRAFT)) {
                Button(onClick = actions.onReturnToDraft, enabled = enabled) { Text("Исправить и отправить заново") }
            }
            if (state.can(LeaveAction.EDIT)) {
                OutlinedButton(onClick = actions.onEdit, enabled = enabled) { Text("Изменить") }
            }
            if (state.can(LeaveAction.CANCEL)) {
                OutlinedButton(onClick = actions.onCancel, enabled = enabled) { Text("Отозвать") }
            }
            if (state.can(LeaveAction.DELETE)) {
                OutlinedButton(
                    onClick = actions.onDelete,
                    enabled = enabled,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text("Удалить", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun PersonRow(label: String, employee: Employee?, onOpenEmployee: (String) -> Unit) {
    if (employee == null) {
        InfoRow(label, "—")
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenEmployee(employee.id) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EmployeeAvatar(employee, size = 40.dp)
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(employee.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(employee.position, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

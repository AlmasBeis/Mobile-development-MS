package kz.alfasoft.mycompany.ui.approvals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.alfasoft.mycompany.ui.AppViewModelProvider
import kz.alfasoft.mycompany.ui.components.ApprovalCard
import kz.alfasoft.mycompany.ui.components.RejectDialog

@Composable
fun ApprovalsScreen(
    modifier: Modifier = Modifier,
    viewModel: ApprovalsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Выбранная вкладка и открытый диалог отклонения — UI-состояние, переживает поворот.
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var rejectingId by rememberSaveable { mutableStateOf<Int?>(null) }
    var reason by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Ожидают (${state.pending.size})") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("История") })
        }
        val items = if (tab == 0) state.pending else state.history
        if (items.isEmpty() && state.loaded) {
            Text(
                if (tab == 0) "Новых заявок нет 🎉" else "Решений пока нет",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
            )
        }
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.request.id }) { item ->
                ApprovalCard(
                    item = item,
                    onApprove = { viewModel.approve(item.request.id) },
                    onReject = {
                        reason = ""
                        rejectingId = item.request.id
                    },
                    processing = state.processingId == item.request.id,
                    actionsEnabled = state.processingId == null,
                )
            }
        }
    }

    rejectingId?.let { id ->
        val name = state.pending.firstOrNull { it.request.id == id }?.employee?.displayName ?: "№$id"
        RejectDialog(
            employeeName = name,
            reason = reason,
            onReasonChange = { reason = it },
            onConfirm = {
                viewModel.reject(id, reason)
                rejectingId = null
            },
            onDismiss = { rejectingId = null },
        )
    }
}

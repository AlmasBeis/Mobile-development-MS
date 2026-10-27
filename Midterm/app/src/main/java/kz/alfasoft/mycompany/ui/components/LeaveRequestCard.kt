package kz.alfasoft.mycompany.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kz.alfasoft.mycompany.data.model.LeaveRequest
import kz.alfasoft.mycompany.data.model.LeaveStatus
import kz.alfasoft.mycompany.data.model.LeaveType

fun leaveTypeLabel(type: LeaveType) = when (type) {
    LeaveType.ANNUAL -> "Ежегодный оплачиваемый"
    LeaveType.UNPAID -> "Без сохранения з/п"
}

@Composable
fun LeaveRequestCard(
    request: LeaveRequest,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    cancelling: Boolean = false,
    cancelEnabled: Boolean = true,
) {
    SectionCard(modifier = modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "№${request.id} · ${formatPeriod(request.start, request.end)}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            StatusChip(request.status)
        }
        Text(
            "${leaveTypeLabel(request.type)} · ${daysLabel(request.days)}",
            style = MaterialTheme.typography.bodyLarge,
        )
        request.decisionComment?.let {
            Text(
                "Комментарий руководителя: $it",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (request.status == LeaveStatus.PENDING) {
            OutlinedButton(onClick = onCancel, enabled = cancelEnabled && !cancelling) {
                Text(if (cancelling) "Отзываем…" else "Отозвать")
            }
        }
    }
}

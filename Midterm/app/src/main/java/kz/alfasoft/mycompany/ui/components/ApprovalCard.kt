package kz.alfasoft.mycompany.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.alfasoft.mycompany.data.model.LeaveStatus
import kz.alfasoft.mycompany.ui.approvals.ApprovalItem
import java.time.LocalDate

private fun date(d: LocalDate) = "%02d.%02d.%d".format(d.dayOfMonth, d.monthValue, d.year)

/** [actionsEnabled] = false, пока выполняется запрос по любой из заявок. */
@Composable
fun ApprovalCard(
    item: ApprovalItem,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
    processing: Boolean = false,
    actionsEnabled: Boolean = true,
) {
    val request = item.request
    SectionCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EmployeeAvatar(item.employee, size = 40.dp)
            Text(
                item.employee.displayName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (request.status != LeaveStatus.PENDING) StatusChip(request.status)
        }
        Text(
            "${formatPeriod(request.start, request.end)} · ${daysLabel(request.days)}",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            "${leaveTypeLabel(request.type)} · подана ${date(request.createdAt)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (request.comment.isNotBlank()) Text("Комментарий: ${request.comment}", style = MaterialTheme.typography.bodyMedium)
        item.substitute?.let { Text("Замещает: ${it.displayName}", style = MaterialTheme.typography.bodyMedium) }

        if (request.status == LeaveStatus.PENDING) {
            Text(
                "Остаток сотрудника после согласования: ${daysLabel(item.balanceAfter)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (item.conflicts.isNotEmpty()) {
                Text(
                    "⚠ В эти даты в отпуске: ${item.conflicts.joinToString { it.displayName }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onApprove, enabled = actionsEnabled && !processing) {
                    Text(if (processing) "Подождите…" else "Согласовать")
                }
                OutlinedButton(onClick = onReject, enabled = actionsEnabled && !processing) { Text("Отклонить") }
            }
        } else {
            request.decidedAt?.let {
                Text("Решение принято ${date(it)}", style = MaterialTheme.typography.bodyMedium)
            }
            request.decisionComment?.let {
                Text("Комментарий руководителя: $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

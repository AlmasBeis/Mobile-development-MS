package kz.alfasoft.mycompany.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RejectDialog(
    employeeName: String,
    reason: String,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Отклонить заявку: $employeeName") },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = onReasonChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Причина (обязательно)") },
                minLines = 2,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = reason.isNotBlank()) { Text("Отклонить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

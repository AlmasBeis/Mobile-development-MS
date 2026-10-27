package kz.alfasoft.mycompany.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.alfasoft.mycompany.data.model.LeaveStatus

@Composable
fun StatusChip(status: LeaveStatus, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val (label, container, content) = when (status) {
        LeaveStatus.PENDING -> Triple("На согласовании", scheme.tertiaryContainer, scheme.onTertiaryContainer)
        LeaveStatus.APPROVED -> Triple("Согласовано", scheme.secondaryContainer, scheme.onSecondaryContainer)
        LeaveStatus.REJECTED -> Triple("Отклонено", scheme.errorContainer, scheme.onErrorContainer)
        LeaveStatus.CANCELLED -> Triple("Отозвано", scheme.surfaceContainerHigh, scheme.onSurfaceVariant)
    }
    Surface(modifier = modifier, shape = MaterialTheme.shapes.small, color = container, contentColor = content) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

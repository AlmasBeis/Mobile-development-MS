package cinema.booking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun SeatGrid(
    capacity: Int,
    occupied: Set<Int>,
    selected: Int?,
    enabled: Boolean,
    onSeatSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "SCREEN",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val spacing = 6.dp
                val seatSize = 34.dp
                val columns = ((maxWidth + spacing) / (seatSize + spacing)).toInt().coerceIn(6, 10)
                Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                    (1..capacity).chunked(columns).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                            row.forEach { number ->
                                SeatCell(
                                    number = number,
                                    taken = number in occupied,
                                    selected = number == selected,
                                    enabled = enabled,
                                    onClick = { onSeatSelected(number) },
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendSwatch("Free", MaterialTheme.colorScheme.surfaceVariant)
            LegendSwatch("Selected", MaterialTheme.colorScheme.primary)
            LegendSwatch("Taken", MaterialTheme.colorScheme.surfaceContainerHighest)
        }
    }
}

@Composable
private fun SeatCell(
    number: Int,
    taken: Boolean,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val background = when {
        selected -> MaterialTheme.colorScheme.primary
        taken -> MaterialTheme.colorScheme.surfaceContainerHighest
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        taken -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .semantics { contentDescription = "Seat $number" }
            .clickable(enabled = enabled && !taken, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = content,
        )
    }
}

@Composable
private fun LegendSwatch(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

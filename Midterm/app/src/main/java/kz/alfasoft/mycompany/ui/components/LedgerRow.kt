package kz.alfasoft.mycompany.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kz.alfasoft.mycompany.data.model.LedgerEntry

@Composable
fun LedgerRow(entry: LedgerEntry, modifier: Modifier = Modifier) {
    val date = entry.date
    val sign = if (entry.days > 0) "+" else "−"
    val amountColor = if (entry.days > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(entry.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                "%02d.%02d.%d".format(date.dayOfMonth, date.monthValue, date.year),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$sign${kotlin.math.abs(entry.days)}",
                style = MaterialTheme.typography.titleMedium,
                color = amountColor,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "остаток ${entry.balanceAfter}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

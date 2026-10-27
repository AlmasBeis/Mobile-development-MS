package kz.alfasoft.mycompany.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kz.alfasoft.mycompany.data.model.LeaveBalance

@Composable
fun BalanceCard(balance: LeaveBalance, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier, title = "Баланс отпуска") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BalanceItem("Начислено", balance.accrued)
            BalanceItem("Использовано", balance.used)
            BalanceItem("На согл.", balance.reserved)
            BalanceItem("Доступно", balance.available, highlighted = true)
        }
    }
}

@Composable
private fun RowScope.BalanceItem(label: String, value: Int, highlighted: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text(
            value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = if (highlighted) FontWeight.Bold else null,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
        )
    }
}

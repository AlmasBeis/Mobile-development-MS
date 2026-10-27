package kz.alfasoft.mycompany.ui.org

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.alfasoft.mycompany.domain.OrgRow
import kz.alfasoft.mycompany.ui.components.EmployeeAvatar

@Composable
fun HierarchyTree(
    rows: List<OrgRow>,
    currentUserId: String?,
    onToggle: (String) -> Unit,
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit,
    onOpenEmployee: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onExpandAll) { Text("Развернуть всё") }
            TextButton(onClick = onCollapseAll) { Text("Свернуть всё") }
        }
        LazyColumn(contentPadding = PaddingValues(horizontal = 8.dp)) {
            items(rows, key = { it.employee.id }) { row ->
                TreeRow(
                    row = row,
                    highlighted = row.employee.id == currentUserId,
                    onToggle = { onToggle(row.employee.id) },
                    onOpen = { onOpenEmployee(row.employee.id) },
                )
            }
        }
    }
}

@Composable
private fun TreeRow(row: OrgRow, highlighted: Boolean, onToggle: () -> Unit, onOpen: () -> Unit) {
    val background = if (highlighted) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, MaterialTheme.shapes.small)
            .clickable(onClick = onOpen)
            .padding(start = (row.depth * 20).dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (row.subordinatesCount > 0) {
            IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (row.expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (row.expanded) "Свернуть" else "Развернуть",
                )
            }
        } else {
            Box(Modifier.size(40.dp))
        }
        EmployeeAvatar(row.employee, size = 40.dp)
        Column(Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
            Text(row.employee.displayName, style = MaterialTheme.typography.titleMedium)
            Text(
                row.employee.position,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (row.subordinatesCount > 0) {
                Text(
                    "подчинённых: ${row.subordinatesCount}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

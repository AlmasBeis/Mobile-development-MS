package kz.alfasoft.mycompany.ui.org

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.alfasoft.mycompany.ui.components.EmployeeListItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmployeeList(
    state: OrgUiState,
    query: String,
    departmentId: String?,
    onQueryChange: (String) -> Unit,
    onDepartmentSelected: (String?) -> Unit,
    onOpenEmployee: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true,
            placeholder = { Text("ФИО, должность или почта") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистить поиск")
                    }
                }
            },
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = departmentId == null,
                    onClick = { onDepartmentSelected(null) },
                    label = { Text("Все") },
                )
            }
            items(state.departmentChips, key = { it.id }) { department ->
                FilterChip(
                    selected = departmentId == department.id,
                    onClick = { onDepartmentSelected(department.id) },
                    label = { Text(department.name) },
                )
            }
        }
        Text(
            "Найдено: ${state.foundCount}",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (state.foundCount == 0) {
            Text(
                "Никого не найдено. Попробуйте изменить запрос.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
            state.groups.forEach { (letter, employees) ->
                stickyHeader(key = "header-$letter") {
                    Text(
                        letter.toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                items(employees, key = { it.id }) { employee ->
                    EmployeeListItem(
                        employee = employee,
                        onClick = { onOpenEmployee(employee.id) },
                        badge = state.onLeaveUntil[employee.id]?.let { "В отпуске до ${shortDate(it)}" },
                    )
                }
            }
        }
    }
}

private fun shortDate(date: java.time.LocalDate) = "%02d.%02d".format(date.dayOfMonth, date.monthValue)

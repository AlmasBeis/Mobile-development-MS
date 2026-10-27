package kz.alfasoft.mycompany.ui.org

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.alfasoft.mycompany.domain.OrgTree
import kz.alfasoft.mycompany.ui.AppViewModelProvider

private val modes = listOf("Все сотрудники", "Иерархия")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgScreen(
    onOpenEmployee: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrgViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val departmentId by viewModel.departmentId.collectAsStateWithLifecycle()

    // Чисто UI-состояние: режим и раскрытые узлы. null = «ещё не трогали», берём путь до текущего пользователя.
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var userExpanded by rememberSaveable { mutableStateOf<List<String>?>(null) }
    val expanded = userExpanded?.toSet() ?: state.defaultExpanded

    Column(modifier = modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(16.dp)) {
            modes.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = mode == index,
                    onClick = { mode = index },
                    shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                ) { Text(label) }
            }
        }
        if (mode == 0) {
            EmployeeList(
                state = state,
                query = query,
                departmentId = departmentId,
                onQueryChange = viewModel::onQueryChange,
                onDepartmentSelected = viewModel::onDepartmentSelected,
                onOpenEmployee = onOpenEmployee,
                modifier = Modifier.weight(1f),
            )
        } else {
            val rows = remember(state.allEmployees, expanded) { OrgTree.flatten(state.allEmployees, expanded) }
            HierarchyTree(
                rows = rows,
                currentUserId = state.currentUserId,
                onToggle = { id -> userExpanded = (if (id in expanded) expanded - id else expanded + id).toList() },
                onExpandAll = { userExpanded = state.allExpandable.toList() },
                onCollapseAll = { userExpanded = emptyList() },
                onOpenEmployee = onOpenEmployee,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

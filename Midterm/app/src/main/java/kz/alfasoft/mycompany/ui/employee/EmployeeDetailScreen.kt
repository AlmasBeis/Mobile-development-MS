package kz.alfasoft.mycompany.ui.employee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.alfasoft.mycompany.ui.AppViewModelProvider
import kz.alfasoft.mycompany.ui.profile.ProfileContent

@Composable
fun EmployeeDetailScreen(
    onBack: () -> Unit,
    onOpenEmployee: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmployeeDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text("Карточка сотрудника", style = MaterialTheme.typography.titleMedium)
        }
        when {
            state.employee != null -> ProfileContent(
                state = state,
                onOpenEmployee = onOpenEmployee,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            state.loaded -> Text("Сотрудник не найден", modifier = Modifier.padding(16.dp))
        }
    }
}

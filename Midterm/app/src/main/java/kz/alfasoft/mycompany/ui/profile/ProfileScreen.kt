package kz.alfasoft.mycompany.ui.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.alfasoft.mycompany.ui.AppViewModelProvider

@Composable
fun ProfileScreen(
    onOpenEmployee: (String) -> Unit,
    onOpenLeave: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(
        state = state,
        onOpenEmployee = onOpenEmployee,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        state.balance?.let { VacationWidget(it.available, onClick = onOpenLeave) }
        OutlinedButton(onClick = onLogout) { Text("Выйти") }
    }
}

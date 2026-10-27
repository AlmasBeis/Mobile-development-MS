package kz.alfasoft.mycompany.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.alfasoft.mycompany.data.model.Employee
import kz.alfasoft.mycompany.ui.AppViewModelProvider
import kz.alfasoft.mycompany.ui.components.EmployeeAvatar
import kz.alfasoft.mycompany.ui.components.SectionCard

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val users by viewModel.demoUsers.collectAsStateWithLifecycle()
    LoginContent(
        users = users,
        onUserSelected = { id ->
            viewModel.login(id)
            onLoggedIn()
        },
        modifier = modifier,
    )
}

@Composable
private fun LoginContent(
    users: List<Employee>,
    onUserSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("MyCompany", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Кадровый портал ТОО «Альфа Софт»",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Text("Войти как", style = MaterialTheme.typography.titleMedium)
        users.forEach { user ->
            SectionCard(modifier = Modifier.clickable { onUserSelected(user.id) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    EmployeeAvatar(user)
                    Column {
                        Text(user.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            user.position,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

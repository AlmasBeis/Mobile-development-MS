package kz.alfasoft.mycompany.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import kotlin.reflect.KClass

data class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val label: String,
    val icon: ImageVector,
)

val topLevelDestinations = listOf(
    TopLevelDestination(Home, Home::class, "Главная", Icons.Default.Home),
    TopLevelDestination(Leave, Leave::class, "Отпуска", Icons.Default.DateRange),
    TopLevelDestination(Org, Org::class, "Оргструктура", Icons.AutoMirrored.Filled.List),
    TopLevelDestination(Approvals, Approvals::class, "Согласования", Icons.Default.CheckCircle),
    TopLevelDestination(Profile, Profile::class, "Профиль", Icons.Default.Person),
)

fun NavDestination?.isIn(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true

fun NavDestination?.showsBottomBar(): Boolean =
    this != null && (topLevelDestinations.any { isIn(it) } || hasRoute(Employee::class))

@Composable
fun BottomBar(
    current: NavDestination?,
    destinations: List<TopLevelDestination>,
    onSelect: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = current.isIn(destination),
                onClick = { onSelect(destination) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = {
                    // На пяти вкладках длинные подписи иначе переносятся по буквам.
                    Text(
                        destination.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                    )
                },
            )
        }
    }
}

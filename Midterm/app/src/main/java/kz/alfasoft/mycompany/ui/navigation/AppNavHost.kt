package kz.alfasoft.mycompany.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import kz.alfasoft.mycompany.ui.AppViewModelProvider
import kz.alfasoft.mycompany.ui.approvals.ApprovalsScreen
import kz.alfasoft.mycompany.ui.employee.EmployeeDetailScreen
import kz.alfasoft.mycompany.ui.home.HomeScreen
import kz.alfasoft.mycompany.ui.leave.LeaveScreen
import kz.alfasoft.mycompany.ui.leaveform.LeaveFormScreen
import kz.alfasoft.mycompany.ui.login.LoginScreen
import kz.alfasoft.mycompany.ui.org.OrgScreen
import kz.alfasoft.mycompany.ui.profile.ProfileScreen

private fun NavHostController.navigateToTab(destination: TopLevelDestination) {
    // Home - корень стека вкладок: к нему просто возвращаемся. Через navigate(Home) с popUpTo(Home)
    // после отправки заявки с главной ничего не происходило.
    if (destination.route == Home && popBackStack(Home, inclusive = false, saveState = true)) return
    navigate(destination.route) {
        popUpTo(Home) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavHost(appViewModel: AppViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val appState by appViewModel.uiState.collectAsStateWithLifecycle()

    // Сессия закончилась (выход или перезапуск процесса) — возвращаемся на экран входа.
    LaunchedEffect(appState.userId, currentDestination) {
        if (appState.userId == null && currentDestination != null && !currentDestination.hasRoute<Login>()) {
            navController.navigate(Login) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val visibleTabs = topLevelDestinations.filter { it.route != Approvals || appState.isManager }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentDestination.showsBottomBar()) {
                BottomBar(
                    current = currentDestination,
                    destinations = visibleTabs,
                    onSelect = navController::navigateToTab,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Login,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { -it / 3 } + fadeOut() },
            popEnterTransition = { slideInHorizontally { -it / 3 } + fadeIn() },
            popExitTransition = { slideOutHorizontally { it } + fadeOut() },
        ) {
            composable<Login> {
                LoginScreen(onLoggedIn = {
                    navController.navigate(Home) {
                        popUpTo<Login> { inclusive = true }
                    }
                })
            }
            composable<Home> {
                HomeScreen(
                    onRequestLeave = { navController.navigate(LeaveNew) },
                    onOpenApprovals = { navController.navigateToTab(topLevelDestinations.first { it.route == Approvals }) },
                )
            }
            composable<Leave> { LeaveScreen(onNewRequest = { navController.navigate(LeaveNew) }) }
            composable<LeaveNew> {
                LeaveFormScreen(
                    onBack = navController::popBackStack,
                    onSubmitted = { approverName ->
                        // Без saveState/restoreState: иначе вместе с «Отпусками» вернулась бы и форма.
                        navController.navigate(Leave) {
                            popUpTo(Home)
                            launchSingleTop = true
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar("Заявка отправлена на согласование: $approverName")
                        }
                    },
                )
            }
            composable<Org> { OrgScreen(onOpenEmployee = { navController.navigate(Employee(it)) }) }
            composable<Employee> {
                EmployeeDetailScreen(
                    onBack = navController::popBackStack,
                    onOpenEmployee = { navController.navigate(Employee(it)) },
                )
            }
            composable<Approvals> { ApprovalsScreen() }
            composable<Profile> {
                ProfileScreen(
                    onOpenEmployee = { navController.navigate(Employee(it)) },
                    onOpenLeave = { navController.navigateToTab(topLevelDestinations.first { it.route == Leave }) },
                    onLogout = appViewModel::logout,
                )
            }
        }
    }
}

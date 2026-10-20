package cinema.booking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cinema.booking.ui.CinemaActions
import cinema.booking.ui.CinemaScreen
import cinema.booking.ui.CinemaViewModel
import cinema.booking.ui.theme.CinemaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            CinemaTheme {
                val viewModel: CinemaViewModel = viewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                CinemaScreen(
                    state = state,
                    actions = CinemaActions(
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        onGenreSelected = viewModel::onGenreSelected,
                        onAffordableOnlyChange = viewModel::onAffordableOnlyChange,
                        onMovieSelected = viewModel::onMovieSelected,
                        onCustomerNameChange = viewModel::onCustomerNameChange,
                        onStudentChange = viewModel::onStudentChange,
                        onSeatSelected = viewModel::onSeatSelected,
                        onPaymentIsCardChange = viewModel::onPaymentIsCardChange,
                        onCardBalanceChange = viewModel::onCardBalanceChange,
                        onBook = viewModel::book,
                        onApplySuggestedCustomer = viewModel::applySuggestedCustomer,
                        onMessageShown = viewModel::onMessageShown,
                    ),
                )
            }
        }
    }
}

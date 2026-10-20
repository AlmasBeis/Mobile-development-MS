package cinema.booking.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import cinema.booking.data.AFFORDABLE_TICKET_LIMIT
import cinema.booking.data.catalogGenres
import cinema.booking.data.formatPrice
import cinema.booking.ui.components.BookingCard
import cinema.booking.ui.components.MovieCard
import cinema.booking.ui.components.RevenueBanner
import cinema.booking.ui.components.SeatGrid
import cinema.booking.ui.components.SectionHeader
import cinema.booking.ui.components.StateSurvivalCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaScreen(
    state: CinemaUiState,
    actions: CinemaActions,
) {
    var postersGlow by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.message

    LaunchedEffect(message?.id) {
        if (message != null) {
            snackbarHostState.showSnackbar(message.text)
            actions.onMessageShown(message.id)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Cinema Booking") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Pick a film, choose a free seat, and pay by card or cash. Students pay 20% less.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                StateSurvivalCard(
                    postersGlow = postersGlow,
                    onPostersGlowChange = { postersGlow = it },
                )
            }
            item {
                SectionHeader(
                    title = "Films",
                    subtitle = if (state.isLoadingCatalog) {
                        "Loading the catalog"
                    } else {
                        "${state.visibleMovies.size} showing"
                    },
                )
            }
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = actions.onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Search") },
                    placeholder = { Text("Film title") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null)
                    },
                    trailingIcon = if (state.searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { actions.onSearchQueryChange("") }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                            }
                        }
                    } else {
                        null
                    },
                )
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = state.selectedGenre == null,
                        onClick = { actions.onGenreSelected(null) },
                        label = { Text("All") },
                    )
                    catalogGenres.forEach { genre ->
                        FilterChip(
                            selected = state.selectedGenre == genre,
                            onClick = { actions.onGenreSelected(genre) },
                            label = { Text(genre.title) },
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = state.affordableOnly,
                        onCheckedChange = actions.onAffordableOnlyChange,
                    )
                    Text("Up to ${formatPrice(AFFORDABLE_TICKET_LIMIT)}")
                }
            }
            if (state.isLoadingCatalog) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Loading the catalog")
                    }
                }
            } else if (state.visibleMovies.isEmpty()) {
                item {
                    Text(
                        text = "No films match this search.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                state.visibleMovies.forEach { movie ->
                    item(key = "movie-${movie.id}") {
                        MovieCard(
                            movie = movie,
                            selected = state.selectedMovieId == movie.id,
                            glow = postersGlow,
                            onClick = { actions.onMovieSelected(movie.id) },
                        )
                    }
                    if (state.selectedMovieId == movie.id) {
                        item(key = "booking-${movie.id}") {
                            BookingForm(state = state, actions = actions)
                        }
                    }
                }
            }
            val selectedHidden = state.selectedMovie != null &&
                state.visibleMovies.none { it.id == state.selectedMovieId }
            if (selectedHidden) {
                item(key = "booking-hidden") {
                    BookingForm(state = state, actions = actions)
                }
            }
            item {
                SectionHeader(
                    title = "Tickets",
                    subtitle = if (state.bookings.isEmpty()) {
                        "No bookings yet"
                    } else {
                        "${state.bookings.size} completed"
                    },
                )
            }
            item {
                RevenueBanner(
                    ticketCount = state.bookings.size,
                    revenue = formatPrice(state.revenue),
                )
            }
            if (state.bookings.isEmpty()) {
                item {
                    Text(
                        text = "A booked seat is marked taken and cannot be sold again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                state.bookings.forEach { booking ->
                    item(key = "ticket-${booking.id}") {
                        BookingCard(booking = booking)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingForm(
    state: CinemaUiState,
    actions: CinemaActions,
) {
    val movie = state.selectedMovie ?: return
    val due = state.quotedPrice() ?: movie.ticketPrice
    val discount = movie.ticketPrice - due
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = movie.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${movie.genre} · ${movie.durationMinutes} min · ${state.freeSeats(movie)} seats free",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = "Customer", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                suggestedCustomers.forEach { customer ->
                    val selected = state.customerName == customer.name && state.isStudent == customer.student
                    FilterChip(
                        selected = selected,
                        onClick = { actions.onApplySuggestedCustomer(customer.name, customer.student) },
                        enabled = !state.isBooking,
                        label = { Text(customer.label) },
                    )
                }
            }
            OutlinedTextField(
                value = state.customerName,
                onValueChange = actions.onCustomerNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isBooking,
                label = { Text("Customer name") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.isStudent,
                    onCheckedChange = actions.onStudentChange,
                    enabled = !state.isBooking,
                )
                Column {
                    Text("Student ticket")
                    Text(
                        text = "20% off the listed price",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(text = "Seat", style = MaterialTheme.typography.labelLarge)
            SeatGrid(
                capacity = movie.hallCapacity,
                occupied = state.occupiedSeats[movie.id].orEmpty(),
                selected = state.selectedSeat,
                enabled = !state.isBooking,
                onSeatSelected = actions.onSeatSelected,
            )
            Text(text = "Payment", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.paymentIsCard,
                    onClick = { actions.onPaymentIsCardChange(true) },
                    enabled = !state.isBooking,
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    label = { Text("Card") },
                )
                SegmentedButton(
                    selected = !state.paymentIsCard,
                    onClick = { actions.onPaymentIsCardChange(false) },
                    enabled = !state.isBooking,
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    label = { Text("Cash") },
                )
            }
            if (state.paymentIsCard) {
                OutlinedTextField(
                    value = state.cardBalance,
                    onValueChange = actions.onCardBalanceChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isBooking,
                    label = { Text("Card balance") },
                    supportingText = { Text("Declined when the balance is below the price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
            PriceLine(label = "Ticket", value = formatPrice(movie.ticketPrice))
            if (state.isStudent && discount > 0.5) {
                PriceLine(label = "Student discount", value = "−${formatPrice(discount)}")
            }
            PriceLine(label = "Due now", value = formatPrice(due), emphasize = true)
            Button(
                onClick = actions.onBook,
                enabled = !state.isBooking,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isBooking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Processing payment")
                } else {
                    Text("Book · ${formatPrice(due)}")
                }
            }
        }
    }
}

@Composable
private fun PriceLine(
    label: String,
    value: String,
    emphasize: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(
            text = value,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

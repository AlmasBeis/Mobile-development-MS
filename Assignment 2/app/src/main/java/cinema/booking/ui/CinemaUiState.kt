package cinema.booking.ui

import cinema.booking.data.Booking
import cinema.booking.data.Customer
import cinema.booking.data.Genre
import cinema.booking.data.Movie
import cinema.booking.data.RegularCustomer
import cinema.booking.data.StudentCustomer

data class UiMessage(val id: Long, val text: String)

data class CinemaUiState(
    val movies: List<Movie> = emptyList(),
    val visibleMovies: List<Movie> = emptyList(),
    val searchQuery: String = "",
    val selectedGenre: Genre? = null,
    val affordableOnly: Boolean = false,
    val selectedMovieId: Int? = null,
    val customerName: String = "",
    val isStudent: Boolean = false,
    val selectedSeat: Int? = null,
    val paymentIsCard: Boolean = true,
    val cardBalance: String = "10000",
    val bookings: List<Booking> = emptyList(),
    val occupiedSeats: Map<Int, Set<Int>> = emptyMap(),
    val isBooking: Boolean = false,
    val isLoadingCatalog: Boolean = true,
    val message: UiMessage? = null,
) {
    val selectedMovie: Movie?
        get() = movies.find { it.id == selectedMovieId }

    val revenue: Double
        get() = bookings
            .map { it.finalPrice }
            .reduceOrNull { total, price -> total + price }
            ?: 0.0

    fun quotedPrice(): Double? {
        val movie = selectedMovie ?: return null
        val customer: Customer = if (isStudent) {
            StudentCustomer(0, customerName)
        } else {
            RegularCustomer(0, customerName)
        }
        return customer.applyDiscount(movie.ticketPrice)
    }

    fun freeSeats(movie: Movie): Int {
        val taken = occupiedSeats[movie.id]?.size ?: 0
        return (movie.hallCapacity - taken).coerceAtLeast(0)
    }
}

class CinemaActions(
    val onSearchQueryChange: (String) -> Unit,
    val onGenreSelected: (Genre?) -> Unit,
    val onAffordableOnlyChange: (Boolean) -> Unit,
    val onMovieSelected: (Int) -> Unit,
    val onCustomerNameChange: (String) -> Unit,
    val onStudentChange: (Boolean) -> Unit,
    val onSeatSelected: (Int) -> Unit,
    val onPaymentIsCardChange: (Boolean) -> Unit,
    val onCardBalanceChange: (String) -> Unit,
    val onBook: () -> Unit,
    val onApplySuggestedCustomer: (String, Boolean) -> Unit,
    val onMessageShown: (Long) -> Unit,
)

data class SuggestedCustomer(val name: String, val student: Boolean, val label: String)

val suggestedCustomers: List<SuggestedCustomer> = listOf(
    SuggestedCustomer("Алия", student = false, label = "Алия · Regular"),
    SuggestedCustomer("Данияр", student = true, label = "Данияр · Student"),
)

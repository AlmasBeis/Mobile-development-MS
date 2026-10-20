package cinema.booking.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cinema.booking.data.AFFORDABLE_TICKET_LIMIT
import cinema.booking.data.BookingRequest
import cinema.booking.data.BookingResult
import cinema.booking.data.CardPayment
import cinema.booking.data.CashPayment
import cinema.booking.data.CinemaService
import cinema.booking.data.Customer
import cinema.booking.data.Genre
import cinema.booking.data.MovieCatalog
import cinema.booking.data.PaymentMethod
import cinema.booking.data.RegularCustomer
import cinema.booking.data.StudentCustomer
import cinema.booking.data.formatPrice
import cinema.booking.data.loadMovies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CinemaViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(restore())
    val uiState: StateFlow<CinemaUiState> = _uiState.asStateFlow()

    private var catalog: MovieCatalog? = null
    private var service: CinemaService? = null
    private var messageCounter = 0L

    init {
        viewModelScope.launch {
            val movies = loadMovies()
            val loadedCatalog = MovieCatalog(movies)
            catalog = loadedCatalog
            service = CinemaService(loadedCatalog)
            publish { it.copy(isLoadingCatalog = false, movies = movies) }
        }
    }

    fun onSearchQueryChange(query: String) {
        publish { it.copy(searchQuery = query) }
    }

    fun onGenreSelected(genre: Genre?) {
        publish { it.copy(selectedGenre = genre) }
    }

    fun onAffordableOnlyChange(enabled: Boolean) {
        publish { it.copy(affordableOnly = enabled) }
    }

    fun onMovieSelected(movieId: Int) {
        publish { current ->
            val deselect = current.selectedMovieId == movieId
            current.copy(
                selectedMovieId = if (deselect) null else movieId,
                selectedSeat = null,
            )
        }
    }

    fun onCustomerNameChange(name: String) {
        publish { it.copy(customerName = name) }
    }

    fun onStudentChange(student: Boolean) {
        publish { it.copy(isStudent = student) }
    }

    fun onSeatSelected(seat: Int) {
        publish { it.copy(selectedSeat = seat) }
    }

    fun onPaymentIsCardChange(card: Boolean) {
        publish { it.copy(paymentIsCard = card) }
    }

    fun onCardBalanceChange(balance: String) {
        publish { it.copy(cardBalance = balance) }
    }

    fun applySuggestedCustomer(name: String, student: Boolean) {
        publish { it.copy(customerName = name, isStudent = student) }
    }

    fun onMessageShown(id: Long) {
        publish { current ->
            if (current.message?.id == id) current.copy(message = null) else current
        }
    }

    fun book() {
        val snapshot = _uiState.value
        if (snapshot.isBooking || snapshot.isLoadingCatalog) return
        val movie = snapshot.selectedMovie ?: return
        val name = snapshot.customerName.trim()
        val seat = snapshot.selectedSeat
        if (name.isEmpty()) {
            showMessage("Enter the customer name")
            return
        }
        if (seat == null) {
            showMessage("Choose a seat")
            return
        }
        val cinema = service ?: return
        val payment = cardOrCash(snapshot) ?: return
        val customer: Customer = if (snapshot.isStudent) {
            StudentCustomer(snapshot.bookings.size + 1, name)
        } else {
            RegularCustomer(snapshot.bookings.size + 1, name)
        }

        viewModelScope.launch {
            publish { it.copy(isBooking = true) }
            try {
                when (val result = cinema.bookTicket(BookingRequest(customer, movie.id, seat, payment))) {
                    is BookingResult.Success -> {
                        val booking = result.booking
                        publish {
                            it.copy(
                                isBooking = false,
                                bookings = cinema.completedBookings(),
                                occupiedSeats = cinema.occupiedSeatsSnapshot(),
                                selectedSeat = null,
                                message = UiMessage(
                                    nextMessageId(),
                                    "${booking.customerName}, seat ${booking.seatNumber}, " +
                                        "${formatPrice(booking.finalPrice)}, ${booking.paymentMethod}",
                                ),
                            )
                        }
                    }

                    is BookingResult.Failure -> {
                        publish {
                            it.copy(
                                isBooking = false,
                                occupiedSeats = cinema.occupiedSeatsSnapshot(),
                                message = UiMessage(nextMessageId(), result.reason),
                            )
                        }
                    }
                }
            } finally {
                if (_uiState.value.isBooking) {
                    publish { it.copy(isBooking = false) }
                }
            }
        }
    }

    private fun cardOrCash(snapshot: CinemaUiState): PaymentMethod? {
        if (!snapshot.paymentIsCard) return CashPayment()
        val balance = snapshot.cardBalance.trim().replace(',', '.').toDoubleOrNull()
        if (balance == null) {
            showMessage("Enter a valid card balance")
            return null
        }
        return CardPayment(balance)
    }

    private fun showMessage(text: String) {
        publish { it.copy(message = UiMessage(nextMessageId(), text)) }
    }

    private fun nextMessageId(): Long = ++messageCounter

    private fun publish(transform: (CinemaUiState) -> CinemaUiState) {
        _uiState.update { current ->
            val base = transform(current)
            val allMovies = catalog?.allMovies() ?: base.movies
            base.copy(
                movies = allMovies,
                visibleMovies = filterMovies(base),
            )
        }
        persist(_uiState.value)
    }

    private fun filterMovies(state: CinemaUiState) = catalog?.findMovies { movie ->
        val matchesQuery = state.searchQuery.isBlank() ||
            movie.title.contains(state.searchQuery, ignoreCase = true)
        val matchesGenre = state.selectedGenre == null || movie.genre == state.selectedGenre
        val matchesPrice = !state.affordableOnly || movie.ticketPrice <= AFFORDABLE_TICKET_LIMIT
        matchesQuery && matchesGenre && matchesPrice
    } ?: emptyList()

    private fun restore(): CinemaUiState = CinemaUiState(
        searchQuery = savedStateHandle.get<String>(KEY_SEARCH).orEmpty(),
        selectedGenre = genreFromTitle(savedStateHandle.get<String>(KEY_GENRE)),
        affordableOnly = savedStateHandle.get<Boolean>(KEY_AFFORDABLE) ?: false,
        selectedMovieId = savedStateHandle.get<Int>(KEY_MOVIE),
        customerName = savedStateHandle.get<String>(KEY_NAME).orEmpty(),
        isStudent = savedStateHandle.get<Boolean>(KEY_STUDENT) ?: false,
        selectedSeat = savedStateHandle.get<Int>(KEY_SEAT),
        paymentIsCard = savedStateHandle.get<Boolean>(KEY_CARD) ?: true,
        cardBalance = savedStateHandle.get<String>(KEY_BALANCE) ?: "10000",
        isLoadingCatalog = true,
    )

    private fun persist(state: CinemaUiState) {
        savedStateHandle[KEY_SEARCH] = state.searchQuery
        savedStateHandle[KEY_GENRE] = state.selectedGenre?.title
        savedStateHandle[KEY_AFFORDABLE] = state.affordableOnly
        savedStateHandle[KEY_MOVIE] = state.selectedMovieId
        savedStateHandle[KEY_NAME] = state.customerName
        savedStateHandle[KEY_STUDENT] = state.isStudent
        savedStateHandle[KEY_SEAT] = state.selectedSeat
        savedStateHandle[KEY_CARD] = state.paymentIsCard
        savedStateHandle[KEY_BALANCE] = state.cardBalance
    }

    private fun genreFromTitle(title: String?): Genre? = when (title) {
        Genre.Action.title -> Genre.Action
        Genre.Comedy.title -> Genre.Comedy
        Genre.Drama.title -> Genre.Drama
        Genre.ScienceFiction.title -> Genre.ScienceFiction
        else -> null
    }

    private companion object {
        const val KEY_SEARCH = "search"
        const val KEY_GENRE = "genre"
        const val KEY_AFFORDABLE = "affordable"
        const val KEY_MOVIE = "movie"
        const val KEY_NAME = "name"
        const val KEY_STUDENT = "student"
        const val KEY_SEAT = "seat"
        const val KEY_CARD = "card"
        const val KEY_BALANCE = "balance"
    }
}

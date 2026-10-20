package cinema.booking.data

import kotlinx.coroutines.delay
import java.util.Locale

class MovieCatalog(private val movies: List<Movie>) {
    val moviesById: Map<Int, Movie> = movies.associateBy { it.id }
    val genres: Set<Genre> = movies.map { it.genre }.toSet()

    fun findMovies(predicate: (Movie) -> Boolean): List<Movie> = movies.filter(predicate)

    fun allMovies(): List<Movie> = movies.toList()
}

class CinemaService(private val catalog: MovieCatalog) {
    private val occupiedSeats: MutableMap<Int, MutableSet<Int>> = mutableMapOf()
    private val bookings: MutableList<Booking> = mutableListOf()

    suspend fun bookTicket(request: BookingRequest): BookingResult {
        val movie = catalog.moviesById[request.movieId]
            ?: return BookingResult.Failure("No film with id ${request.movieId}")

        if (request.seatNumber !in 1..movie.hallCapacity) {
            return BookingResult.Failure("Seat ${request.seatNumber} is not in this hall")
        }

        val movieSeats = occupiedSeats.getOrPut(movie.id) { mutableSetOf() }
        if (request.seatNumber in movieSeats) {
            return BookingResult.Failure("Seat ${request.seatNumber} is already taken")
        }

        val finalPrice = request.customer.applyDiscount(movie.ticketPrice)
        if (!request.paymentMethod.pay(finalPrice)) {
            return BookingResult.Failure("Payment of ${formatPrice(finalPrice)} was declined")
        }

        movieSeats.add(request.seatNumber)
        val booking = Booking(
            id = bookings.size + 1,
            customerName = request.customer.name,
            movieTitle = movie.title,
            seatNumber = request.seatNumber,
            finalPrice = finalPrice,
            paymentMethod = request.paymentMethod.displayName,
        )
        bookings.add(booking)
        return BookingResult.Success(booking)
    }

    fun completedBookings(): List<Booking> = bookings.toList()

    fun occupiedSeatsSnapshot(): Map<Int, Set<Int>> =
        occupiedSeats.mapValues { (_, seats) -> seats.toSet() }
}

suspend fun loadMovies(): List<Movie> {
    pause(250)
    return listOf(
        Movie(1, "Человек-бензопила: История Резе", Genre.Action, 169, 3_500.0, 40),
        Movie(2, "Человек-паук: Новый день", Genre.ScienceFiction, 112, 2_800.0, 35),
        Movie(3, "Одиссея", Genre.Drama, 148, 3_200.0, 45),
        Movie(4, "Мстители: Финал", Genre.Action, 130, 2_600.0, 30),
        Movie(5, "Полночь в Алматы", Genre.Comedy, 104, 2_400.0, 32),
    )
}

suspend fun pause(milliseconds: Long) {
    delay(milliseconds)
}

fun formatPrice(price: Double): String = String.format(Locale.US, "%.0f ₸", price)

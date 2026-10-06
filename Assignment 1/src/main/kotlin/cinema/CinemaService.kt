package cinema

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
            ?: return BookingResult.Failure("Фильм с id ${request.movieId} не найден")

        if (request.seatNumber !in 1..movie.hallCapacity) {
            return BookingResult.Failure("Место ${request.seatNumber} отсутствует в зале")
        }

        val movieSeats = occupiedSeats.getOrPut(movie.id) { mutableSetOf() }
        if (request.seatNumber in movieSeats) {
            return BookingResult.Failure("Место ${request.seatNumber} уже занято")
        }

        val finalPrice = request.customer.applyDiscount(movie.ticketPrice)
        if (!request.paymentMethod.pay(finalPrice)) {
            return BookingResult.Failure("Оплата на сумму ${formatPrice(finalPrice)} отклонена")
        }

        movieSeats.add(request.seatNumber)
        val booking = Booking(
            id = bookings.size + 1,
            customerName = request.customer.name,
            movieTitle = movie.title,
            seatNumber = request.seatNumber,
            finalPrice = finalPrice,
            paymentMethod = request.paymentMethod.displayName
        )
        bookings.add(booking)
        return BookingResult.Success(booking)
    }

    fun completedBookings(): List<Booking> = bookings.toList()
}

suspend fun loadMovies(): List<Movie> {
    pause(250)
    return listOf(
        Movie(1, "Человек-бензопила: История Резе", Genre.Action, 169, 3_500.0, 40),
        Movie(2, "Человек-паук: Новый день", Genre.ScienceFiction, 112, 2_800.0, 35),
        Movie(3, "Одиссея", Genre.Drama, 148, 3_200.0, 45),
        Movie(4, "Мстители: Финал", Genre.Action, 130, 2_600.0, 30)
    )
}

suspend fun pause(milliseconds: Long) {
    suspendCoroutine { continuation ->
        Thread {
            Thread.sleep(milliseconds)
            continuation.resume(Unit)
        }.start()
    }
}

fun formatPrice(price: Double): String = "%.0f ₸".format(price)

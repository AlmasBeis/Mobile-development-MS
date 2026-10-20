package cinema.booking.data

const val AFFORDABLE_TICKET_LIMIT = 3_000.0

sealed class Genre(val title: String) {
    object Action : Genre("Action")
    object Comedy : Genre("Comedy")
    object Drama : Genre("Drama")
    object ScienceFiction : Genre("Sci-fi")

    override fun toString(): String = title
}

val catalogGenres: List<Genre> = listOf(
    Genre.Action,
    Genre.Comedy,
    Genre.Drama,
    Genre.ScienceFiction,
)

data class Movie(
    val id: Int,
    val title: String,
    val genre: Genre,
    val durationMinutes: Int,
    val ticketPrice: Double,
    val hallCapacity: Int,
)

data class Booking(
    val id: Int,
    val customerName: String,
    val movieTitle: String,
    val seatNumber: Int,
    val finalPrice: Double,
    val paymentMethod: String,
)

data class BookingRequest(
    val customer: Customer,
    val movieId: Int,
    val seatNumber: Int,
    val paymentMethod: PaymentMethod,
)

sealed class BookingResult {
    data class Success(val booking: Booking) : BookingResult()
    data class Failure(val reason: String) : BookingResult()
}

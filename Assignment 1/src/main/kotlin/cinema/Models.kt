package cinema

sealed class Genre(val title: String) {
    object Action : Genre("Боевик")
    object Comedy : Genre("Комедия")
    object Drama : Genre("Драма")
    object ScienceFiction : Genre("Фантастика")

    override fun toString(): String = title
}

data class Movie(
    val id: Int,
    val title: String,
    val genre: Genre,
    val durationMinutes: Int,
    val ticketPrice: Double,
    val hallCapacity: Int
)

data class Booking(
    val id: Int,
    val customerName: String,
    val movieTitle: String,
    val seatNumber: Int,
    val finalPrice: Double,
    val paymentMethod: String
)

data class BookingRequest(
    val customer: Customer,
    val movieId: Int,
    val seatNumber: Int,
    val paymentMethod: PaymentMethod
)

sealed class BookingResult {
    data class Success(val booking: Booking) : BookingResult()
    data class Failure(val reason: String) : BookingResult()
}

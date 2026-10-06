package cinema

suspend fun main() {
    println("Система бронирования кинотеатра")
    println("Загрузка каталога...")

    val movies: List<Movie> = loadMovies()
    val catalog = MovieCatalog(movies)
    val cinemaService = CinemaService(catalog)

    println("\nКаталог фильмов:")
    printMovies(catalog.allMovies()) { movie ->
        "${movie.id}. ${movie.title} | ${movie.genre} | ${movie.durationMinutes} мин. | ${formatPrice(movie.ticketPrice)}"
    }

    val affordableMovies = catalog.findMovies { it.ticketPrice <= 3_000.0 }
    val affordableTitles = affordableMovies.map { it.title }
    val totalCatalogPrice = movies.map { it.ticketPrice }.reduce { total, price -> total + price }

    println("\nФильмы до 3000 ₸: ${affordableTitles.joinToString()}")
    println("Жанры в каталоге: ${catalog.genres.joinToString()}")
    println("Сумма цен всех билетов: ${formatPrice(totalCatalogPrice)}")

    val customers: List<Customer> = listOf(
        RegularCustomer(1, "Алия"),
        StudentCustomer(2, "Данияр")
    )

    val featuredMovie = catalog.moviesById.getValue(1)
    println("\nКлиенты и цены на фильм «${featuredMovie.title}»:")
    for (customer in customers) {
        val price = customer.applyDiscount(featuredMovie.ticketPrice)
        println("${customer.name} (${customer.category}): ${formatPrice(price)}")
    }

    val requests: List<BookingRequest> = listOf(
        BookingRequest(customers[0], 1, 12, CardPayment(10_000.0)),
        BookingRequest(customers[1], 1, 13, CashPayment()),
        BookingRequest(customers[1], 1, 12, CardPayment(10_000.0)),
        BookingRequest(customers[0], 3, 7, CardPayment(1_000.0))
    )

    println("\nРезультаты бронирования:")
    var requestIndex: Int = 0
    while (requestIndex < requests.size) {
        when (val result = cinemaService.bookTicket(requests[requestIndex])) {
            is BookingResult.Success -> {
                val booking = result.booking
                println(
                    "Успешно: ${booking.customerName}, «${booking.movieTitle}», " +
                        "место ${booking.seatNumber}, ${formatPrice(booking.finalPrice)}, ${booking.paymentMethod}"
                )
            }

            is BookingResult.Failure -> println("Ошибка: ${result.reason}")
        }
        requestIndex++
    }

    val revenue = cinemaService.completedBookings()
        .map { it.finalPrice }
        .reduceOrNull { total, price -> total + price }
        ?: 0.0

    println("\nУспешных бронирований: ${cinemaService.completedBookings().size}")
    println("Общая выручка: ${formatPrice(revenue)}")
}

fun printMovies(movies: List<Movie>, formatter: (Movie) -> String) {
    movies.forEach { movie -> println(formatter(movie)) }
}

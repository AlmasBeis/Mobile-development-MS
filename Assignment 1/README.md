# Cinema Booking

A console Kotlin application for booking cinema tickets: a movie catalog, customer discounts, and seat payment.

## What was done

1. Variables and the types `Int`, `String`, `Double`, and `Boolean` are used in the project. `if` conditions check the seat and payment in `CinemaService.bookTicket`, and `when` handles the result in `Main.kt`. Loops: `for` over customers and `while` over booking requests in `Main.kt`.
2. `List` holds the catalog, customers, and requests. `Set` holds genres and occupied seats. `Map` holds movies by id (`moviesById`) and seats by movie (`occupiedSeats`) in `CinemaService.kt`. `map` collects titles and prices, `filter` selects movies cheaper than 3000 ₸ in `findMovies`, and `reduce` and `reduceOrNull` calculate the total catalog price and revenue in `Main.kt`.
3. `printMovies` and `findMovies` take a function as an argument. Lambdas are passed to `printMovies`, `findMovies`, `map`, `filter`, and `reduce`.
4. Classes: `MovieCatalog`, `CinemaService`, `RegularCustomer`, and `CardPayment`. Genre objects: `Genre.Action`, `Genre.Comedy`, `Genre.Drama`, and `Genre.ScienceFiction` in `Models.kt`.
5. `RegularCustomer` and `StudentCustomer` inherit from `Customer` in `Customers.kt`.
6. `DiscountPolicy` defines the discount, and `PaymentMethod` defines payment. Calls to `applyDiscount` and `pay` work for a regular customer, a student, a card, and cash.
7. Data classes `Movie`, `Booking`, and `BookingRequest` are in `Models.kt`.
8. Sealed classes `Genre` and `BookingResult` are in `Models.kt`. `BookingResult` distinguishes a successful booking from a failure.
9. Suspend functions: `main`, `loadMovies`, `bookTicket`, and `pay`. `pause` in `CinemaService.kt` suspends execution with `suspendCoroutine` and resumes it after the delay.

## Build and run

JDK 25 is required.

macOS and Linux:

```bash
./gradlew run
```

Windows:

```bat
gradlew.bat run
```

In IntelliJ IDEA, open the project folder, wait for the Gradle sync, and select JDK 25. Then run `main` in `src/main/kotlin/cinema/Main.kt`.

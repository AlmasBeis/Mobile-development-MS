package cinema.booking.data

interface PaymentMethod {
    val displayName: String
    suspend fun pay(amount: Double): Boolean
}

class CardPayment(private val availableBalance: Double) : PaymentMethod {
    override val displayName: String = "Card"

    override suspend fun pay(amount: Double): Boolean {
        pause(600)
        return availableBalance >= amount
    }
}

class CashPayment : PaymentMethod {
    override val displayName: String = "Cash"

    override suspend fun pay(amount: Double): Boolean {
        pause(400)
        return amount > 0
    }
}

package cinema

interface PaymentMethod {
    val displayName: String
    suspend fun pay(amount: Double): Boolean
}

class CardPayment(private val availableBalance: Double) : PaymentMethod {
    override val displayName: String = "Банковская карта"

    override suspend fun pay(amount: Double): Boolean {
        pause(150)
        return availableBalance >= amount
    }
}

class CashPayment : PaymentMethod {
    override val displayName: String = "Наличные"

    override suspend fun pay(amount: Double): Boolean {
        pause(100)
        return amount > 0
    }
}

package cinema

interface DiscountPolicy {
    fun applyDiscount(price: Double): Double
}

abstract class Customer(
    val id: Int,
    val name: String
) : DiscountPolicy {
    abstract val category: String
}

class RegularCustomer(id: Int, name: String) : Customer(id, name) {
    override val category: String = "Обычный клиент"

    override fun applyDiscount(price: Double): Double = price
}

class StudentCustomer(id: Int, name: String) : Customer(id, name) {
    override val category: String = "Студент"

    override fun applyDiscount(price: Double): Double = price * 0.8
}

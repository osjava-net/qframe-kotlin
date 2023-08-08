package net.osjava.qframe.database

class Order {
    internal val items: MutableList<OrderItem> = mutableListOf();

    constructor()

    constructor(key: String, asc: Boolean) {
        items.add(OrderItem(key, asc));
    }

    companion object {
        fun asc(key: String): Order = Order(key, true)
        fun desc(key: String): Order = Order(key, false)

        fun empty(): Order = Order();
    }

    fun append(key: String, asc: Boolean):Order {
        items.add(OrderItem(key, asc))
        return this
    }
    data class OrderItem(val key: String, val asc: Boolean)
}


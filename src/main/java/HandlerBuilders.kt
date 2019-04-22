/**
 * TODO: Maybe builder-style would be a better choice?
 * Resembles Rx-style to some extent.
 */
val f = on<UnderlyingPrices>().updated { before, after ->
    val mid = (after.bid + after.offer) / 2
    after.options.forEach {
        val (bid, ask) = calculatePrice(mid, it.maturesIn, it.strike, after.interestRate, after.volatility)
        it.update().bid = bid
        it.update().offer = ask
    }
}

fun <T> on(): HandlingScope<T> {
    TODO("not implemented")
}

interface HandlingScope<T> {
    fun updated(reaction: (T, T) -> Unit)
    fun created(reaction: (T) -> Unit)
    fun deleted(reaction: (T) -> Unit)
}

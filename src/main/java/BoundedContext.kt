/**
 * Another option to access repositories is build common facade with compile
 * time tooling. Every application may explicitly opt which of the entities
 * defined in the schema it is going to use.
 */
interface BoundedContext {
    val instruments: Repository<Instrument>
    val quotes: Repository<Quote>
    val derivatives: Repository<Derivative>
    val pricingRequest: Repository<PricingRequest>
}

/**
 * Financial instrument
 */
interface Instrument {
    val isin: ISIN
    var pricesValid: Boolean
    var bidPrice: Price
    var bidVolume: Long
    var offerPrice: Price
    var offerVolume: Long
    val quote: Quote
    val derivatives: List<Derivative>
}

/**
 * Real-time market state for the instrument
 */
interface Quote {
    val isin: ISIN
    val bidPrice: Price
    val bidVolume: Long
    val offerPrice: Price
    val offerVolume: Long
    /**
     * If relationships are made cheap enough (challenging),
     * most of the finders can be skipped, using navigation instead.
     */
    val instrument: Instrument
}

/**
 * Derivative of the instrument
 */
interface Derivative {
    val instrument: Instrument
    var bidPrice: Price
    var offerPrice: Price
}

/**
 * A volatile entity - only exists in memory.
 * The repository is implemented as a queue.
 * Instance is automatically removed upon handlers completion.
 */
interface PricingRequest {
    val derivative: Derivative
    val instrumentBidPrice: Price
    val instrumentOfferPrice: Price
}

val boundedContext: BoundedContext = object: BoundedContext {
    override val pricingRequest: Repository<PricingRequest>
        get() = TODO("not implemented")
    override val instruments: Repository<Instrument>
        get() = TODO("not implemented")
    override val quotes: Repository<Quote>
        get() = TODO("not implemented")
    override val derivatives: Repository<Derivative>
        get() = TODO("not implemented")
}

@OnUpdate
fun onMarketMove(
    q: Quote
) {
    val quote = boundedContext.quotes.find(q.isin)
    quote?.apply {
        val instrument = quote.instrument
        instrument.apply {
            this.bidPrice = quote.bidPrice
            this.bidVolume = quote.bidVolume
            this.offerPrice = quote.offerPrice
            this.offerVolume = quote.offerVolume
        }
    }
}

@OnUpdate
fun onInstrumentPriceMove(
    i: Instrument
) {
    if (!i.pricesValid) return
    i.derivatives.forEach { d ->
        boundedContext.pricingRequest.new(object: PricingRequest {
            override val derivative: Derivative
                get() = d
            override val instrumentBidPrice: Price
                get() = i.bidPrice
            override val instrumentOfferPrice: Price
                get() = i.offerPrice
        })
    }
}

@OnCreate
fun onPricingRequest(pr: PricingRequest) {
    val price = blackScholes(pr.instrumentBidPrice, pr.instrumentOfferPrice)
    val spread = pr.derivative.offerPrice - pr.derivative.bidPrice
    pr.derivative.bidPrice = price - spread / 2
    pr.derivative.offerPrice = price + spread / 2
}

fun blackScholes(bidPrice: Price, offerPrice: Price): Price {
    // imaginary computation based on mid-price
    return (bidPrice + offerPrice) /  2 * 0.001
}

@file:Suppress("unused")

typealias ID = Long
typealias Price = Double
typealias RIC = String
typealias ISIN = String
typealias TimeInYears = Double
typealias Rate = Double

/**
 * Every entity is represented as an interface. No concrete classes.
 * Additionally to giving more freedom in using the entities, this also
 * mitigates the problem of maintaining the relationships.
 * Now there is no need to update both sides of a relationship as usually happens with ORMs.
 * It is taken care of by sharing relationship state between participants,
 * exposing only the corresponding view to each of them.
 */

interface UnderlyingStatic {
    val id: String
    val ric: RIC
    val isin: ISIN
    val options : List<Option>
}

interface UnderlyingParams : MarketState {
    var tradeable: Boolean
}

interface UnderlyingDynamic {
    val bid: Price
    val offer: Price

    val volatility : Double

    val delta : Double
    val gamma : Double
    val vega :  Double
    val theta : Double
    val rho : Double
}

interface Underlying : UnderlyingStatic, UnderlyingParams, UnderlyingDynamic

interface OptionStatic {
    val id: String
    val isin: ISIN
    val description: String
    val underlying : Underlying
    val strike : Price
    val expiry : TimeInYears
}

interface OptionParams {
    var tradeable: Boolean
    var suspendReason: String
    var maturesIn : TimeInYears
}

interface OptionDynamic {
    var bid: Price
    var offer: Price
}

interface Option : OptionStatic, OptionParams, OptionDynamic {
    fun update(): Option
}

interface OptionPricingRequest {
    val underlyingBid : Price
    val underltyingOffer : Price
}

interface MarketState {
    val rate : Double
}

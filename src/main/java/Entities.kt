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
 * One big question here is how to separate three differently evolving entity
 * parts - static, semi-static (=semi-volatile if you prefer), and volatile.
 * Particularly, each of this parts are likely to be managed separately.
 * Static data is traditionally managed via sort of control panel application.
 * Semi-static data - with operational dashboard frontend.
 * Volatile data is something that is either a reflection of some constantly changing
 * external state required for the application (e.g. game objects positions) or
 * data derived from the former (prices calculations reflecting market fluctuations).
 * If combine all the three into a single entity it would probably be harder to manipulate
 * with each of them separately, and may require dis-joining of a whole entity in some way.
 * However, if each is kept as an individual class, first of all, naming of that class
 * is not trivial. Secondly there should be a way to identify the attributes on which
 * entities are joined when required. Should they share same id for that purpose?
 */

interface UnderlyingStatic {
    /**
     * TODO: identifiers are actually a very tricky thing -
     * If we let them be of generic type like Long or String, it would not eliminate
     * a quite common mistake of passing an identifiers of another entity type to a
     * function processing different kind of entities.
     */
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
    val mid: Price

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

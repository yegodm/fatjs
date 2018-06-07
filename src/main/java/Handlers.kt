@file:Suppress("unused")

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

/**
 * We can use meta-programming style for handler mark-up.
 * Three type of events need to be supported - creates, updates,
 * and deletes.
 * Since we want our system to keep historical state, instead
 * of traditional "soft" deletes we might opt to do real deletes.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnCreate

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnUpdate

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnDelete


/**
 * We want to react only to those updates
 * where the selected properties have been changed
 * (this is not necessary at the very start but is ultimately required).
 * We want it to be compile-time verified so will be using language
 * features to reference the class properties.
 *
 * To do that with annotation-based approach we'll need to introduce
 * extra class as there seems to be no other way to reference properties
 * in an annotation.
 * Let's call the filter Projection as we are projecting the state
 * to subset of properties.
 */
interface Projection<T> {
    val properties: Collection<KProperty1<T, *>>
}

/**
 * Here is a filter declaration.
 * Only thing it has to provide is the list of property references.
 * In that sense it becomes re-usable so many handlers can share
 * the same projection.
 */
object TradeableFilter : Projection<Underlying> {
    override val properties = listOf(Underlying::tradeable)
}

object PriceFilter : Projection<Underlying> {
    override val properties = listOf(Underlying::bid, Underlying::offer)
}

annotation class PropertyFilter<T, P>
        where P : Projection<T>

@OnUpdate
@PropertyFilter<Underlying, TradeableFilter>
fun underlyingTradeableUpdated(before: Underlying, after: Underlying) {
    for (option in after.options) {
        /**
         * We need to signal to the repository that the option will be updated.
         * What if there'll be a generic update() function that actually does that?
         * It can be invoked multiple times within a handler. Only the first one
         * would actually make a memory allocation.
         */
        option.update()
                .tradeable = after.tradeable
        if (!after.tradeable)
            option.update()
                    .suspendReason = "trading stopped on underlying"
    }
    /**
     * Once handler ends, the system commits all the changes -
     * everything marked as updated becomes the new current state.
     * Committing the state would also trigger those handlers that expect
     * that.
     */
}

@OnUpdate
@PropertyFilter<Underlying, PriceFilter>
fun underlyingPricesUpdated(before: Underlying, after: Underlying) {
    /**
     * Here we want to dispatch multiple simultaneous price calculations
     * for every option based on this underlying.
     */
    /**
     * Somewhere we need to do calculation like this (simplified):
     * after.mid = (after.bid + after.offer) / 2
     * TODO: Another step in workflow that triggers the next one maybe?
     **/
    after.options.forEach {
        OptionPricingRequest::class.new(object : OptionPricingRequest {
            override val underlyingBid = after.bid
            override val underltyingOffer = after.offer
        })
    }
}


@OnDelete
fun deleted(o: Option) {
}

/**
 * From the other point of view, if we declare Projection as a class,
 * maybe handler should be a part of it?
 * TODO: Would we need a class for every handler then?
 */
interface HandlerWithProjection<T> {
    val properties: Array<KProperty1<T, *>>
    fun onCreate(after: T) {}
    fun onUpdate(before: T, after: T) {}
    fun onDelete(before: T) {}
}

/**
 * Or, alternatively, why not use another interface declaration just for the purpose
 * of indicating which properties are in the projection?
 * It remains compatible with the base but carries explicit overrides for those
 * values that are supposed to be projected.
 */
annotation class Projecting

@Projecting
interface UnderlyingPrices : Underlying {
    override val bid: Price
    override val offer: Price
    override val mid: Price
}

interface OptionPrices : Option {
    override var bid: Price
    override var offer: Price
}

val optionPriceFilter = OptionPrices::class.declaredMemberProperties
val underlyingPriceFilter = UnderlyingPrices::class.declaredMemberProperties

interface OptionGreeksRequest {
    val underlyingBid: Price
    val underlyingOffer: Price
    val bid: Price
    val offer: Price
}


fun calculatePrice(
        mid: Double,
        maturesIn: TimeInYears,
        strike: Price,
        rate: Double,
        volatility: Double):
        Pair<Price, Price> {
    TODO("not implemented")
}

/**
 * It is possible to declare all entities as immutable using {@code val} declarations
 * only. However, it is somewhat challenging to expose instances for update in that case
 * since we need explicit save() operation.
 */
interface Boo {
    val fooRef: Long
    val name: String
    var rooRef: Long
}

interface Foo {
    val id: Long
    val name: String
    val amount: Double
    val active: Boolean
}

@OnUpdate
fun activateFooOnBooUpdate(before: Boo, after: Boo) {
    val foo = from<Foo>().find(after.fooRef)!!
    from<Foo>().save(object : Foo by foo {
        override val active = (after.name == "bigBoo")
    })
}

/**
 * Alternatively we can allow var declarations.
 * That makes it possible to register entity for update either explicitly or
 * implicitly, on a first setter call.
 */
interface FooVar {
    var id: Long
    var name: String
    var amount: Double
    var active: Boolean
}

@OnUpdate
fun activateFooVarOnBooUpdate(before: Boo, after: Boo) {
    val foo = from<FooVar>().find(after.fooRef)!!
    foo.active = after.name == "tinyBoo"
}

/**
 * Likely the best choice is to allow combination of var and
 * val matching the semantics of immutable and mutable properties.
 */
interface Roo {
    val id: Long
    val description: String
    var name: String
    var amount: Double
    var active: Boolean
}

@OnCreate
fun createRooOnNewBoo(after: Boo) {
    val roo = from<Roo>().new(object : Roo {
        override val id = 1234L
        override val description = "created from boo ${after.name}"
        override var name  = after.name
        override var amount = 1.0
        override var active = false
    })
    after.rooRef = roo.id
}

object BooName : Projection<Boo> {
    override val properties = listOf(Boo::name)
}

@OnUpdate
@PropertyFilter<Boo, BooName>
fun updateRooFromBoo(before: Boo, after: Boo) {
    from<Roo>().find(after.rooRef)?.also {  roo ->
        if (after.name != roo.name)
            roo.name = after.name
    }
}
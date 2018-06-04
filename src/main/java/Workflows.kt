/**
 * What if we organize all of the related handlers into a workflow (or Saga)?
 */
interface Workflow {
    val id: ID
}

interface OptionPricing : Workflow {
    /**
     * Then each function representing a step can return a new state that triggers another step(-s).
     * This should actually chain asynchronous activities.
     * TODO: How to deal with exceptions?
     */
    @OnUpdate
    fun underlyingPricesUpdated(before: UnderlyingPrices, after: UnderlyingPrices):
        Sequence<() -> OptionPrices>

    @OnUpdate
    fun optionPricesUpdated(before: OptionPrices, after: OptionPrices):
        Sequence<() -> OptionGreeks>
}

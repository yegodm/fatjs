/**
 * What if we organize all of the related handlers into a workflow (or Saga)?
 */
interface Workflow {
    val id: ID
}

interface OptionPricing : Workflow {
    /**
     * Then each function representing a step can return a new state that triggers
     * another step(-s), and so on. This should actually chain asynchronous activities.
     * TODO: How to deal with exceptions?
     *
     * Or should we let the handler just update the state instead, and then this state
     * change triggers another wave of reactions?
     */
    @OnUpdate
    fun underlyingPricesUpdated(before: UnderlyingPrices, after: UnderlyingPrices)

    @OnUpdate
    fun midPriceCalculated(before: UnderlyingPrices, after: UnderlyingPrices)

    @OnUpdate
    fun optionPricesUpdated(before: OptionPrices, after: OptionPrices)
}

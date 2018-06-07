@file:Suppress("unused")

import kotlin.reflect.KClass

/**
 * Common entry point to get access to the repository of the given type.
 * We should probably avoid Dependency Injection as commonly seen in
 * modern applications when dealing with DAO objects. We don't need it
 * and we don't want it. We also want to avoid having Session-like (first-level cache)
 * objects that track life cycle of materialized entities within of whatever we
 * would call a transaction. Instead, all entities that are critical for business
 * logic will remain in memory for as long as possible, and all updates
 * will be first applied to the in-memory state.
 */
fun <T> from(): Repository<T> {
    TODO("not implemented")
}

/**
 * Repositories are very important. This is the only place where
 * validation can be unambiguously performed. It is exactly one of
 * the Repository responsibilities.
 *
 * Even in case when an external system emits a message that our system
 * should process, we can represent that as creating a new entity in
 * the corresponding strongly-typed repository. And that is where the incoming
 * message would be validated! If it doesn't pass the checks, it gets
 * rejected and no further processing occurs. Yet when the new state is valid,
 * we asynchronously initiate a new workflow as the reaction to this new message.
 *
 * Some repositories may serve as transient queues as in the example with incoming message
 * in the comments above. Every addition causes handlers to be executed and then the state
 * is instantly removed.
 *
 * Since we are not talking about yet another shiny ORM, it makes sense
 * to reconsider the need for separate new() and save() methods.
 * TODO: Maybe having single merge() operation is a better choice?
 */
interface Repository<T> {
    /**
     * Creates a new entity out of the state
     */
    fun new(state : T) : T

    /**
     * Saves the updated state.
     */
    fun save(state : T) : T


    /**
     * Removes the state from the repository
     */
    fun delete(state : T) : Nothing

    /**
     * Retrieves the state revision
     */
    fun revision(state : T) : Long

    fun find(id: Any) : T?
}

inline fun <reified T : Any> KClass<*>.new(state : T): T {
    return from<T>().new(state)
}



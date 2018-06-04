@file:Suppress("unused")

import kotlin.reflect.KClass

/**
 * Common entry point to get access to the repository of the given type.
 * 
 */
fun <T> from(): Repository<T> {
    TODO("not implemented")
}

interface Repository<T> {
    /**
     * Creates a new entity out of the state
     */
    fun new(state : T) : T

    /**
     * Discards the state from the repository
     */
    fun delete(state : T) : Nothing

    /**
     * Retrieves the state revision
     */
    fun revision(state : T) : Long
}

inline fun <reified T : Any> KClass<*>.new(state : T): T {
    return from<T>().new(state)
}

/**
 * Some repositories may serve as transient queues.
 * Every addition causes handlers to be executed and then the state is instantly removed.
 */

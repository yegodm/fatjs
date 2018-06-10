package relationships

/**
 * Relationships between entities are defined in a natural way.
 */
interface Class {
    val attending: MutableList<Student>
    val enrolled: MutableList<Student>
}

interface Student {
    var currentClass: Class
    var nextClass: Class
}

fun Student.assign(currentClass: Class, nextClass: Class) {
    this.currentClass = currentClass
    this.nextClass = nextClass
    /**
     * Both sides are managed by updating the shared state.
     */
    assert(currentClass.attending.contains(this))
    assert(nextClass.enrolled.contains(this))
}
/**
 * TODO: However, there should be a way to distinguish which of relationships
 * is represented by which property. That way we would know what two properties
 * share the internal state.
 */


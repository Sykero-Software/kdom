package fi.sykero.kdom.internal

/**
 * A read-only view of [list], kdom's replacement for `java.util.Collections.unmodifiableList`: every mutator
 * (reachable from Java, or through a cast to `MutableList` on the JVM) throws `UnsupportedOperationException`, and
 * `equals`/`hashCode`/`toString` follow the `List` contract of the wrapped list.
 */
internal class UnmodifiableList<E>(private val list: List<E>) : AbstractList<E>(), RandomAccess {
    override val size: Int get() = list.size

    override fun get(index: Int): E = list[index]
}

/** `Collections.unmodifiableList(list)`. */
internal fun <E> unmodifiableList(list: List<E>): List<E> = UnmodifiableList(list)

/**
 * An empty `MutableMap` whose `put` throws `UnsupportedOperationException`, kdom's replacement for
 * `java.util.Collections.emptyMap()` where jdom2 stored it in a mutable-typed field
 * (`ProcessingInstruction` after unparsable data).
 */
internal class EmptyUnmodifiableMap<K, V> : AbstractMutableMap<K, V>() {
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = LinkedHashSet()

    override fun put(key: K, value: V): V? = throw UnsupportedOperationException()
}

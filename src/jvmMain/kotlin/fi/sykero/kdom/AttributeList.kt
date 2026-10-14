/*-- 

 Copyright (C) 2000-2012 Jason Hunter & Brett McLaughlin.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows 
    these conditions in the documentation and/or other materials 
    provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact <request_AT_jdom_DOT_org>.

 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management <request_AT_jdom_DOT_org>.

 In addition, we request (but do not require) that you include in the 
 end-user documentation provided with the redistribution and/or in the 
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by the
      JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos 
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many 
 individuals on behalf of the JDOM Project and was originally 
 created by Jason Hunter <jhunter_AT_jdom_DOT_org> and
 Brett McLaughlin <brett_AT_jdom_DOT_org>.  For more information
 on the JDOM Project, please see <http://www.jdom.org/>.

 Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software.

 */

package fi.sykero.kdom

import fi.sykero.kdom.internal.KdomMutableList
import fi.sykero.kdom.internal.ArrayCopy

/**
 * `AttributeList` represents legal JDOM
 * `[Attribute]` content.
 *
 * This class is NOT PUBLIC; users should see it as a simple List
 * implementation, although it behaves something like a Set because you cannot
 * add duplicate Attributes. An attribute is considered duplicate if it has the
 * same Namespace URI and Attribute name as another existing Attribute.
 *
 * @author Alex Rosen
 * @author Philippe Riand
 * @author Bradley S. Huffman
 * @author Rolf Lear
 */
internal class AttributeList
/**
 * Create a new instance of the AttributeList representing *parent*
 * Element's Attributes
 *
 * @param parent
 *        Element whose Attributes are to be held
 */
(
    /** The parent Element  */
    private val parent: Element
) : KdomMutableList<Attribute>(), RandomAccess {

    /** The backing array  */
    private var attributeData: Array<Attribute?>? = null

    /** The current size  */
    override var size: Int = 0
        private set

    /**
     * Package internal method to support building from sources that are 100%
     * trusted.
     *
     * @param a
     *        an Attribute to add without any checks
     */
    fun uncheckedAddAttribute(a: Attribute) {
        a.parentRef = parent
        ensureCapacity(size + 1)
        attributeData!![size++] = a
        modCount++
    }

    /**
     * Check and add *attribute* to the end of the list or replace an
     * existing `Attribute` with the same name and
     * `Namespace`.
     *
     * @param element
     *        The `Attribute` to insert into the list.
     * @return true as specified by `Collection.add()`.
     * @throws IllegalAddException
     *         if validation rules prevent the add
     */
    override fun add(element: Attribute): Boolean {
        val attribute = element
        if (attribute.parent != null) {
            throw IllegalAddException(
                "The attribute already has an existing parent \""
                        + attribute.parent!!.qualifiedName + "\""
            )
        }

        if (Verifier.checkNamespaceCollision(attribute, parent) != null) {
            throw IllegalAddException(
                parent, attribute,
                Verifier.checkNamespaceCollision(attribute, parent)
            )
        }

        // returns -1 if not exist
        val duplicate = indexOfDuplicate(attribute)
        if (duplicate < 0) {
            attribute.setParent(parent)
            ensureCapacity(size + 1)
            attributeData!![size++] = attribute
            modCount++
        } else {
            val old = attributeData!![duplicate]!!
            old.setParent(null)
            attributeData!![duplicate] = attribute
            attribute.setParent(parent)
        }
        return true
    }

    /**
     * Check and add *attribute* to this list at *index*.
     *
     * @param index
     *        where to add/insert the `Attribute`
     * @param element
     *        `Attribute` to add
     * @throws IllegalAddException
     *         if validation rules prevent the add
     */
    override fun add(index: Int, element: Attribute) {
        val attribute = element
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException(
                "Index: " + index +
                        " Size: " + size
            )
        }

        if (attribute.parent != null) {
            throw IllegalAddException(
                "The attribute already has an existing parent \"" +
                        attribute.parent!!.qualifiedName + "\""
            )
        }
        val duplicate = indexOfDuplicate(attribute)
        if (duplicate >= 0) {
            throw IllegalAddException("Cannot add duplicate attribute")
        }

        val reason = Verifier.checkNamespaceCollision(attribute, parent)
        if (reason != null) {
            throw IllegalAddException(parent, attribute, reason)
        }

        attribute.setParent(parent)

        ensureCapacity(size + 1)
        if (index == size) {
            attributeData!![size++] = attribute
        } else {
            attributeData!!.copyInto(attributeData!!, index + 1, index, index + (size - index))
            attributeData!![index] = attribute
            size++
        }
        modCount++
    }

    /**
     * Add all the `Attributes` in *collection*.
     *
     * @param elements
     *        The `Collection` of `Attributes` to add.
     * @return `true` if the list was modified as a result of the
     *         add.
     * @throws IllegalAddException
     *         if validation rules prevent the addAll
     */
    override fun addAll(elements: Collection<Attribute>): Boolean {
        return addAll(size, elements)
    }

    /**
     * Inserts the `Attributes` in *collection* at the specified
     * *index* in this list.
     *
     * @param index
     *        The offset at which to start adding the `Attributes`
     * @param elements
     *        The `Collection` containing the `Attributes`
     *        to add.
     * @return `true` if the list was modified as a result of the
     *         add.
     * @throws IllegalAddException
     *         if validation rules prevent the addAll
     */
    override fun addAll(index: Int, elements: Collection<Attribute>): Boolean {
        val collection: Collection<Attribute>? = elements
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException(
                "Index: " + index +
                        " Size: " + size
            )
        }

        if (collection == null) {
            throw NullPointerException(
                "Can not add a null Collection to AttributeList"
            )
        }
        val addcnt = collection.size
        if (addcnt == 0) {
            return false
        }
        if (addcnt == 1) {
            // quick check for single-add.
            add(index, collection.iterator().next())
            return true
        }

        ensureCapacity(size + addcnt)

        val tmpmodcount = modCount
        var ok = false

        var count = 0

        try {
            for (att in collection) {
                add(index + count, att)
                count++
            }
            ok = true
        } finally {
            if (!ok) {
                while (--count >= 0) {
                    removeAt(index + count)
                }
                modCount = tmpmodcount
            }
        }

        return true
    }

    /**
     * Clear the current list.
     */
    override fun clear() {
        if (attributeData != null) {
            while (size > 0) {
                size--
                attributeData!![size]!!.setParent(null)
                attributeData!![size] = null
            }
        }
        modCount++
    }

    /**
     * Clear the current list and set it to the contents of *collection*.
     *
     * @param collection
     *        The `Collection` to use.
     * @throws IllegalAddException
     *         if validation rules prevent the addAll
     */
    fun clearAndSet(collection: Collection<Attribute>?) {
        if (collection == null || collection.isEmpty()) {
            clear()
            return
        }

        // keep a backup in case we need to roll-back...
        val old = attributeData
        val oldSize = size
        val oldModCount = modCount

        // clear the current system
        // we need to detach before we add so that we don't run in to a problem
        // where an attribute in the to-add list is one that we are 'clearing'
        // first.
        while (size > 0) {
            old!![--size]!!.setParent(null)
        }
        size = 0
        attributeData = null

        var ok = false
        try {
            addAll(0, collection)
            ok = true
        } finally {
            if (!ok) {
                // we have an exception pending....
                // restore the old system.
                // re-attach the old stuff
                attributeData = old
                while (size < oldSize) {
                    attributeData!![size++]!!.setParent(parent)
                }
                modCount = oldModCount
            }
        }

    }

    /**
     * Increases the capacity of this `AttributeList` instance, if
     * necessary, to ensure that it can hold at least the number of items
     * specified by the minimum capacity argument.
     *
     * @param minCapacity
     *        the desired minimum capacity.
     */
    private fun ensureCapacity(minCapacity: Int) {
        val data = attributeData
        if (data == null) {
            attributeData = arrayOfNulls<Attribute>(Math.max(minCapacity, INITIAL_ARRAY_SIZE))
            return
        } else if (minCapacity < data.size) {
            return
        }
        // most JVM's allocate memory in multiples of 'double-words', on
        // 64-bit it's 16-bytes, on 32-bit it's 8 bytes which all means it makes
        // sense to increment the capacity in even values.
        attributeData = ArrayCopy.copyOf(
            data,
            ((minCapacity + INITIAL_ARRAY_SIZE) ushr 1) shl 1
        )
    }

    /**
     * Retrieve the `Attribute` at *offset*.
     *
     * @param index
     *        The position of the `Attribute` to retrieve.
     * @return The `Attribute` at position *index*.
     */
    override fun get(index: Int): Attribute {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException(
                "Index: " + index +
                        " Size: " + size
            )
        }

        return attributeData!![index]!!
    }

    /**
     * Retrieve the `Attribute` with the given name and the same
     * `Namespace` URI as *namespace*.
     *
     * @param name
     *        name of attribute to return
     * @param namespace
     *        indicate what `Namespace` URI to consider
     * @return the `Attribute`, or null if one doesn't exist.
     */
    fun get(name: String?, namespace: Namespace?): Attribute? {
        val index = indexOf(name, namespace)
        if (index < 0) {
            return null
        }
        return attributeData!![index]
    }

    /**
     * Return index of the `Attribute` with the given *name* and
     * the same Namespace URI as *namespace*.
     *
     * @param name
     *        name of `Attribute` to retrieve
     * @param namespace
     *        indicate what `Namespace` URI to consider
     * @return the index of the attribute that matches the conditions, or
     *         `-1` if there is none.
     */
    fun indexOf(name: String?, namespace: Namespace?): Int {
        if (attributeData != null) {
            if (namespace == null) {
                return indexOf(name, Namespace.NO_NAMESPACE)
            }
            val uri = namespace.uri
            for (i in 0 until size) {
                val att = attributeData!![i]!!
                if (att.namespaceURI == uri &&
                    att.name == name
                ) {
                    return i
                }
            }
        }
        return -1
    }

    /**
     * Remove the `Attribute` at *index*.
     *
     * @param index
     *        The offset of the `Attribute` to remove.
     * @return The removed `Attribute`.
     */
    override fun removeAt(index: Int): Attribute {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException(
                "Index: " + index +
                        " Size: " + size
            )

        val old = attributeData!![index]!!
        old.setParent(null)
        attributeData!!.copyInto(attributeData!!, index, index + 1, index + 1 + (size - index - 1))
        attributeData!![--size] = null // Let gc do its work
        modCount++
        return old
    }

    /**
     * Remove the `Attribute` with the specified name and the same
     * URI as *namespace*.
     *
     * @param name
     *        name of `Attribute` to remove
     * @param namespace
     *        indicate what `Namespace` URI to consider
     * @return the `true` if attribute was removed,
     *         `false` otherwise
     */
    fun remove(name: String?, namespace: Namespace?): Boolean {
        val index = indexOf(name, namespace)
        if (index < 0) {
            return false
        }
        removeAt(index)
        return true
    }

    /**
     * Set the `Attribute` at *index* to be *attribute*.
     *
     * @param index
     *        The location to set the value to.
     * @param element
     *        The `Attribute` to set.
     * @return The replaced `Attribute`.
     * @throws IllegalAddException
     *         if validation rules prevent the set
     */
    override fun set(index: Int, element: Attribute): Attribute {
        val attribute = element
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException(
                "Index: " + index +
                        " Size: " + size
            )

        if (attribute.parent != null) {
            throw IllegalAddException(
                "The attribute already has an existing parent \"" +
                        attribute.parent!!.qualifiedName + "\""
            )
        }

        val duplicate = indexOfDuplicate(attribute)
        if ((duplicate >= 0) && (duplicate != index)) {
            throw IllegalAddException("Cannot set duplicate attribute")
        }

        val reason = Verifier.checkNamespaceCollision(attribute, parent, index)
        if (reason != null) {
            throw IllegalAddException(parent, attribute, reason)
        }

        val old = attributeData!![index]!!
        old.setParent(null)

        attributeData!![index] = attribute
        attribute.setParent(parent)
        return old
    }

    /**
     * Return index of attribute with same name and Namespace, or -1 if one
     * doesn't exist
     */
    private fun indexOfDuplicate(attribute: Attribute): Int {
        return indexOf(attribute.name, attribute.namespace)
    }

    /**
     * Returns an `Iterator` over the `Attributes` in this
     * list in the proper sequence.
     *
     * @return an iterator.
     */
    override fun iterator(): MutableIterator<Attribute> {
        return ALIterator()
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    /**
     * Return this list as a `String`
     */
    override fun toString(): String {
        return super.toString()
    }

    /**
     * Unlike the Arrays.binarySearch, this method never expects an
     * "already exists" condition, we only ever add, thus there will never
     * be a negative insertion-point.
     * @param indexes The pointers to search within
     * @param len The number of pointers to search within
     * @param `val` The pointer we are checking for.
     * @param comp The Comparator to compare with
     * @return the insertion point.
     */
    private fun binarySearch(
        indexes: IntArray, len: Int,
        `val`: Int, comp: Comparator<in Attribute>
    ): Int {
        var left = 0
        var mid = 0
        var right = len - 1
        var cmp = 0
        val base = attributeData!![`val`]
        while (left <= right) {
            mid = (left + right) ushr 1
            cmp = comp.compare(base, attributeData!![indexes[mid]])
            if (cmp == 0) {
                while (cmp == 0 && mid < right && comp.compare(
                        base, attributeData!![indexes[mid + 1]]
                    ) == 0
                ) {
                    mid++
                }
                return mid + 1
            } else if (cmp < 0) {
                right = mid - 1
            } else {
                left = mid + 1
            }
        }
        return left
    }

    private fun sortInPlace(indexes: IntArray) {
        // the indexes are a discrete set of values that have no duplicates,
        // and describe the relative order of each of them.
        // as a result, we can do some tricks....
        val unsorted = ArrayCopy.copyOf(indexes, indexes.size)
        unsorted.sort()
        val usc = arrayOfNulls<Attribute>(unsorted.size)
        for (i in usc.indices) {
            usc[i] = attributeData!![indexes[i]]
        }
        // usc contains the content in their pre-sorted order....
        for (i in indexes.indices) {
            attributeData!![unsorted[i]] = usc[i]
        }
    }

    /**
     * Sort the attributes using the supplied comparator. The attributes are
     * never added using regular mechanisms, so there are never problems with
     * detached or already-attached Attributes. The sort happens 'in place'.
     *
     * If the comparator identifies two (or more) Attributes to be equal, then
     * the relative order of those attributes will not be changed.
     *
     * @param comp The Comparator to use for sorting.
     */
    // kdom: the JVM `sort(Comparator)` override lives in KdomMutableList (common code has no `List.sort` member).
    override fun sortLive(comp: Comparator<in Attribute>?) {
        sortAttributes(comp)
    }

    /**
     * The body of [sort], reachable from Kotlin callers (the JDK `List.sort` member is not callable
     * on a Kotlin `MutableList`, where the `sort` extension is found instead).
     */
    fun sortAttributes(comp: Comparator<in Attribute>?) {
        val comp: Comparator<in Attribute> = comp ?: ATTRIBUTE_NATURAL
        val sz = size
        val indexes = IntArray(sz)
        for (i in 0 until sz) {
            val ip = binarySearch(indexes, i, i, comp)
            if (ip < i) {
                indexes.copyInto(indexes, ip + 1, ip, ip + (i - ip))
            }
            indexes[ip] = i
        }
        sortInPlace(indexes)
    }

    /* * * * * * * * * * * * * ContentListIterator * * * * * * * * * * * * * */
    /* * * * * * * * * * * * * ContentListIterator * * * * * * * * * * * * * */
    /**
     * A fast iterator that can beat AbstractList because we can access the data
     * directly. This is important because so much code now uses the for-each
     * type loop `for (Attribute a : element.getAttributes()) {...}`,
     * and that uses iterator().
     *
     * @author Rolf Lear
     */
    private inner class ALIterator : MutableIterator<Attribute> {
        // The modCount to expect (or throw ConcurrentModeEx)
        private var expect = -1

        // the index of the next Attribute to return.
        private var cursor = 0

        // whether it is legal to call remove()
        private var canremove = false

        init {
            expect = modCount
        }

        override fun hasNext(): Boolean {
            return cursor < size
        }

        override fun next(): Attribute {
            if (modCount != expect) {
                throw ConcurrentModificationException(
                    "ContentList was " +
                            "modified outside of this Iterator"
                )
            }
            if (cursor >= size) {
                throw NoSuchElementException(
                    "Iterated beyond the end of " +
                            "the ContentList."
                )
            }
            canremove = true
            return attributeData!![cursor++]!!
        }

        override fun remove() {
            if (modCount != expect) {
                throw ConcurrentModificationException(
                    "ContentList was " +
                            "modified outside of this Iterator"
                )
            }
            if (!canremove) {
                throw IllegalStateException(
                    "Can only remove() content " +
                            "after a call to next()"
                )
            }
            this@AttributeList.removeAt(--cursor)
            expect = modCount
            canremove = false
        }

    }

    private companion object {
        /** The initial size to start the backing array.  */
        private const val INITIAL_ARRAY_SIZE = 4

        private val ATTRIBUTE_NATURAL: Comparator<Attribute> = object : Comparator<Attribute> {

            override fun compare(a1: Attribute, a2: Attribute): Int {
                val pcomp = a1.namespacePrefix.compareTo(a2.namespacePrefix)
                if (pcomp != 0) {
                    return pcomp
                }
                return a1.name.compareTo(a2.name)
            }

        }
    }

}

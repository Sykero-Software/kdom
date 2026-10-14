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

import fi.sykero.kdom.filter.Filter
import fi.sykero.kdom.internal.ArrayCopy
import java.util.Arrays
import java.util.Comparator
import java.util.ConcurrentModificationException
import java.util.RandomAccess

/**
 * A non-public list implementation holding only legal JDOM content, including
 * content for Document or Element nodes. Users see this class as a simple List
 * implementation.
 *
 * @see DocType
 * @see CDATA
 * @see Comment
 * @see Element
 * @see EntityRef
 * @see ProcessingInstruction
 * @see Text
 * @author Alex Rosen
 * @author Philippe Riand
 * @author Bradley S. Huffman
 * @author Rolf Lear
 */
internal class ContentList
/**
 * Force either a Document or Element parent
 *
 * @param parent
 *        the Element this ContentList belongs to.
 */
(
    /** Document or Element this list belongs to  */
    private val parent: Parent
) : AbstractMutableList<Content>(), RandomAccess {

    /** Our backing list  */
    private var elementData: Array<Content?>? = null

    /**
     * The amount of valid content in elementData
     * (Named `count` here, not `size`: the inner [FilterList] has its own `size`, which would shadow
     * the outer field that Java code reached as plain `size`.)
     */
    private var count: Int = 0

    /**
     * Completely remove references to AbstractList.modCount because in
     * ContentList it is confusing. As a consequence we also need to implement
     * a custom ListIterator for ContentList so that we don't use any of the
     * AbstractList iterators which use modCount.... so we have our own
     * ConcurrentModification checking.
     *
     */
    @Transient
    private var sizeModCount = Int.MIN_VALUE

    /**
     * modCount is used for concurrent modification, but dataModCount is used
     * for refreshing filters.
     */
    @Transient
    private var dataModiCount = Int.MIN_VALUE

    /**
     * Package internal method to support building from sources that are 100%
     * trusted.
     *
     * @param c
     *        content to add without any checks
     */
    fun uncheckedAddContent(c: Content) {
        c.parentRef = parent
        ensureCapacity(count + 1)
        elementData!![count++] = c
        incModCount()
    }

    /**
     * In the FilterList and FilterList iterators it becomes confusing as to
     * which modCount is being used. This formalizes the process, and using
     * (set/get/inc)ModCount() is the only thing you should see in the remainder
     * of this code.
     *
     * @param sizemod
     *        the value to set for the size-mod count.
     * @param datamod
     *        the value to set for the data-mod count.
     */
    private fun setModCount(sizemod: Int, datamod: Int) {
        sizeModCount = sizemod
        dataModiCount = datamod
    }

    /**
     * In the FilterList and FilterList iterators it becomes confusing as to
     * which modCount is being used. This formalizes the process, and using
     * (set/get/inc)ModCount() is the only thing you should see in the remainder
     * of this code.
     *
     * @return mod the value.
     */
    private fun getModCount(): Int {
        return sizeModCount
    }

    /**
     * In the FilterList and FilterList iterators it becomes confusing as to
     * which modCount is being used. This formalizes the process, and using
     * (set/get/inc)ModCount() is the only thing you should see in the remainder
     * of this code.
     */
    private fun incModCount() {
        // indicate there's a change to data
        dataModiCount++
        // indicate there's a change to the size
        sizeModCount++
    }

    private fun incDataModOnly() {
        dataModiCount++
    }

    /**
     * Get the modcount of data changes.
     * @return the current data mode count.
     */
    private fun getDataModCount(): Int {
        return dataModiCount
    }

    private fun checkIndex(index: Int, excludes: Boolean) {
        val max = if (excludes) count - 1 else count

        if (index < 0 || index > max) {
            throw IndexOutOfBoundsException(
                "Index: " + index +
                        " Size: " + count
            )
        }

    }

    private fun checkPreConditions(
        child: Content?, index: Int,
        replace: Boolean
    ) {
        if (child == null) {
            throw NullPointerException("Cannot add null object")
        }

        checkIndex(index, replace)

        if (child.parent != null) {
            // the content to be added already has a parent.
            val p = child.parent
            if (p is Document) {
                throw IllegalAddException(
                    child as Element,
                    "The Content already has an existing parent document"
                )
            }
            throw IllegalAddException(
                "The Content already has an existing parent \"" +
                        (p as Element).qualifiedName + "\""
            )
        }

        if (child === parent) {
            throw IllegalAddException(
                "The Element cannot be added to itself"
            )
        }

        // Detect if we have <a><b><c/></b></a> and c.add(a)
        if ((parent is Element && child is Element) &&
            child.isAncestor(parent)
        ) {
            throw IllegalAddException(
                "The Element cannot be added as a descendent of itself"
            )
        }

    }

    /**
     * Check and add the `Content` to this list at the given index.
     * Inserts the specified object at the specified position in this list.
     * Shifts the object currently at that position (if any) and any subsequent
     * objects to the right (adds one to their indices).
     *
     * @param index
     *        index where to add `Element`
     * @param element
     *        `Content` to add
     */
    override fun add(index: Int, element: Content) {
        val child = element
        // Confirm basic sanity of child.
        checkPreConditions(child, index, false)
        // Check to see whether this parent believes it can contain this content
        parent.canContainContent(child, index, false)

        child.setParent(parent)

        ensureCapacity(count + 1)
        if (index == count) {
            elementData!![count++] = child
        } else {
            System.arraycopy(elementData!!, index, elementData!!, index + 1, count - index)
            elementData!![index] = child
            count++
        }
        // Successful add's increment the AbstractList's modCount
        incModCount()
    }

    /**
     * Add the specified collection to the end of this list.
     *
     * @param elements
     *        The collection to add to the list.
     * @return `true` if the list was modified as a result of the
     *         add.
     */
    override fun addAll(elements: Collection<Content>): Boolean {
        return addAll(count, elements)
    }

    /**
     * Inserts the specified collection at the specified position in this list.
     * Shifts the object currently at that position (if any) and any subsequent
     * objects to the right (adds one to their indices).
     *
     * @param index
     *        The offset to start adding the data in the collection
     * @param elements
     *        The collection to insert into the list.
     * @return `true` if the list was modified as a result of the
     *         add. throws IndexOutOfBoundsException if index < 0 || index >
     *         size()
     */
    override fun addAll(
        index: Int,
        elements: Collection<Content>
    ): Boolean {
        val collection: Collection<Content>? = elements
        if ((collection == null)) {
            throw NullPointerException(
                "Can not add a null collection to the ContentList"
            )
        }

        checkIndex(index, false)

        if (collection.isEmpty()) {
            // some collections are expensive to get the size of.
            // use isEmpty().
            return false
        }
        val addcnt = collection.size
        if (addcnt == 1) {
            // quick check for single-add.
            add(index, collection.iterator().next())
            return true
        }

        ensureCapacity(size + addcnt)

        val tmpmodcount = getModCount()
        val tmpdmc = getDataModCount()
        var ok = false

        var cnt = 0

        try {
            for (c in collection) {
                add(index + cnt, c)
                cnt++
            }
            ok = true
        } finally {
            if (!ok) {
                // something failed... remove all added content
                while (--cnt >= 0) {
                    removeAt(index + cnt)
                }
                // restore the mod-counts.
                setModCount(tmpmodcount, tmpdmc)
            }
        }

        return true
    }

    /**
     * Clear the current list.
     */
    override fun clear() {
        if (elementData != null) {
            for (i in 0 until count) {
                val obj = elementData!![i]!!
                removeParent(obj)
            }
            elementData = null
            count = 0
        }
        incModCount()
    }

    /**
     * Clear the current list and set it to the contents of the
     * `Collection`. object.
     *
     * @param collection
     *        The collection to use.
     */
    fun clearAndSet(collection: Collection<Content>?) {
        if (collection == null || collection.isEmpty()) {
            clear()
            return
        }

        // keep a backup in case we need to roll-back...
        val old = elementData
        val oldSize = count
        val oldModCount = getModCount()
        val oldDataModCount = getDataModCount()

        // clear the current system
        // we need to detach before we add so that we don't run in to a problem
        // where a content in the to-add list is one that we are 'clearing'
        // first.
        while (count > 0) {
            old!![--count]!!.setParent(null)
        }
        count = 0
        elementData = null

        var ok = false
        try {
            addAll(0, collection)
            ok = true
        } finally {
            if (!ok) {
                // we have an exception pending....
                // restore the old system.
                // we do not need to worry about the added content
                // because the failed addAll will clear it up.
                // re-attach the old stuff
                elementData = old
                while (count < oldSize) {
                    elementData!![count++]!!.setParent(parent)
                }
                setModCount(oldModCount, oldDataModCount)
            }
        }

    }

    /**
     * Increases the capacity of this `ContentList` instance, if
     * necessary, to ensure that it can hold at least the number of items
     * specified by the minimum capacity argument.
     *
     * @param minCapacity
     *        the desired minimum capacity.
     */
    fun ensureCapacity(minCapacity: Int) {
        val data = elementData
        if (data == null) {
            elementData = arrayOfNulls<Content>(Math.max(minCapacity, INITIAL_ARRAY_SIZE))
            return
        } else if (minCapacity < data.size) {
            return
        }
        // use algorithm Wilf suggests which is essentially the same
        // as algorithm as ArrayList.ensureCapacity....
        // typically the minCapacity is only slightly larger than
        // the current capacity.... so grow from the current capacity
        // with a double-check.
        val newcap = ((count * 3) / 2) + 1
        elementData = ArrayCopy.copyOf(
            data,
            (if (newcap < minCapacity) minCapacity else newcap)
        )
    }

    /**
     * Return the object at the specified offset.
     *
     * @param index
     *        The offset of the object.
     * @return The Object which was returned.
     */
    override fun get(index: Int): Content {
        checkIndex(index, true)
        return elementData!![index]!!
    }

    /**
     * Return a view of this list based on the given filter.
     *
     * @param <E>
     *        The Generic type of the content as set by the Filter.
     * @param filter
     *        `Filter` for this view.
     * @return a list representing the rules of the `Filter`.
     */
    fun <E : Content> getView(filter: Filter<E>): MutableList<E> {
        return FilterList<E>(filter)
    }

    /**
     * Return the index of the first Element in the list. If the parent is a
     * `Document` then the element is the root element. If the list
     * contains no Elements, it returns -1.
     *
     * @return index of first element, or -1 if one doesn't exist
     */
    fun indexOfFirstElement(): Int {
        if (elementData != null) {
            for (i in 0 until count) {
                if (elementData!![i] is Element) {
                    return i
                }
            }
        }
        return -1
    }

    /**
     * Return the index of the DocType element in the list. If the list contains
     * no DocType, it returns -1.
     *
     * @return index of the DocType, or -1 if it doesn't exist
     */
    fun indexOfDocType(): Int {
        if (elementData != null) {
            for (i in 0 until count) {
                if (elementData!![i] is DocType) {
                    return i
                }
            }
        }
        return -1
    }

    /**
     * Remove the object at the specified offset.
     *
     * @param index
     *        The offset of the object.
     * @return The Object which was removed.
     */
    override fun removeAt(index: Int): Content {
        checkIndex(index, true)

        val old = elementData!![index]!!
        removeParent(old)
        System.arraycopy(elementData!!, index + 1, elementData!!, index, count - index - 1)
        elementData!![--count] = null // Let gc do its work
        incModCount()
        return old
    }

    /**
     * Set the object at the specified location to the supplied object.
     *
     * @param index
     *        The location to set the value to.
     * @param element
     *        The location to set the value to.
     * @return The object which was replaced. throws IndexOutOfBoundsException
     *         if index < 0 || index >= size()
     */
    override fun set(index: Int, element: Content): Content {
        val child = element
        // Confirm basic sanity of child.
        checkPreConditions(child, index, true)

        // Ensure the detail checks out OK too.
        parent.canContainContent(child, index, true)

        /*
         * Do a special case of set() where we don't do a remove() then add()
         * because that affects the modCount. We want to do a true set(). See
         * issue #15
         */

        val old = elementData!![index]!!
        removeParent(old)
        child.setParent(parent)
        elementData!![index] = child
        // for set method we increment dataModCount, but not modCount
        // set does not change the structure of the List (size())
        incDataModOnly()
        return old
    }

    /**
     * Return the number of items in this list
     *
     * @return The number of items in this list.
     */
    override val size: Int
        get() = count

    override fun iterator(): MutableIterator<Content> {
        return CLIterator()
    }

    override fun listIterator(): MutableListIterator<Content> {
        return CLListIterator(0)
    }

    override fun listIterator(index: Int): MutableListIterator<Content> {
        return CLListIterator(index)
    }

    /**
     * Return this list as a `String`
     *
     * @return The String representation of this list.
     */
    override fun toString(): String {
        return super.toString()
    }

    private fun sortInPlace(indexes: IntArray) {
        // the indexes are a discrete set of values that have no duplicates,
        // and describe the relative order of each of them.
        // as a result, we can do some tricks....
        val unsorted = ArrayCopy.copyOf(indexes, indexes.size)
        Arrays.sort(unsorted)
        val usc = arrayOfNulls<Content>(unsorted.size)
        for (i in usc.indices) {
            usc[i] = elementData!![indexes[i]]
        }
        // usc contains the content in their pre-sorted order....
        for (i in indexes.indices) {
            elementData!![unsorted[i]] = usc[i]
        }
    }

    /**
     * Unlike the Arrays.binarySearch, this method never expects an
     * "already exists" condition, we only ever add, thus there will never
     * be a negative insertion-point.
     * @param indexes THe pointers to search within
     * @param len The number of pointers to search within
     * @param `val` The pointer we are checking for.
     * @param comp The Comparator to compare with
     * @return the insertion point.
     */
    private fun binarySearch(
        indexes: IntArray, len: Int,
        `val`: Int, comp: Comparator<in Content>
    ): Int {
        var left = 0
        var mid = 0
        var right = len - 1
        var cmp = 0
        val base = elementData!![`val`]
        while (left <= right) {
            mid = (left + right) ushr 1
            cmp = comp.compare(base, elementData!![indexes[mid]])
            if (cmp == 0) {
                while (cmp == 0 && mid < right && comp.compare(
                        base, elementData!![indexes[mid + 1]]
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

    /**
     * Sorts this list using the supplied Comparator to compare elements.
     *
     * @param comp - the Comparator used to compare list elements. A null value indicates that the elements' natural ordering should be used
     * @Since 2.0.6
     */
    // @Override - only in Java8
    override fun sort(comp: Comparator<in Content>?) {
        sortContent(comp)
    }

    /**
     * The body of [sort], reachable from Kotlin callers (the JDK `List.sort` member is not callable
     * on a Kotlin `MutableList`, where the `sort` extension is found instead).
     */
    fun sortContent(comp: Comparator<in Content>?) {

        if (comp == null) {
            // sort by the 'natural order', which, there is none.
            // options, throw exception, or let the current-order represent the natural order.
            // do nothing is the better alternative.
            return
        }

        val sz = count
        val indexes = IntArray(sz)
        for (i in 0 until sz) {
            val ip = binarySearch(indexes, i, i, comp)
            if (ip < i) {
                System.arraycopy(indexes, ip, indexes, ip + 1, i - ip)
            }
            indexes[ip] = i
        }
        sortInPlace(indexes)
    }

    /* * * * * * * * * * * * * ContentListIterator * * * * * * * * * * * * * * * */
    /* * * * * * * * * * * * * ContentListIterator * * * * * * * * * * * * * * * */
    /**
     * A fast implementation of Iterator.
     *
     * It is fast because it is tailored to the ContentList, and not the
     * flexible implementation used by AbstractList. It needs to be fast because
     * iterator() is used extensively in the for-each type loop.
     *
     * @author Rolf Lear
     */
    private inner class CLIterator : MutableIterator<Content> {
        private var expect = -1
        private var cursor = 0
        private var canremove = false

        init {
            expect = getModCount()
        }

        override fun hasNext(): Boolean {
            return cursor < count
        }

        override fun next(): Content {
            if (getModCount() != expect) {
                throw ConcurrentModificationException(
                    "ContentList was " +
                            "modified outside of this Iterator"
                )
            }
            if (cursor >= count) {
                throw NoSuchElementException(
                    "Iterated beyond the end of " +
                            "the ContentList."
                )
            }
            canremove = true
            return elementData!![cursor++]!!
        }

        override fun remove() {
            if (getModCount() != expect) {
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
            canremove = false
            this@ContentList.removeAt(--cursor)
            expect = getModCount()
        }

    }

    /* * * * * * * * * * * * * ContentListIterator * * * * * * * * * * * * * * * */
    /* * * * * * * * * * * * * ContentListIterator * * * * * * * * * * * * * * * */
    /**
     * A fast implementation of Iterator.
     *
     * It is fast because it is tailored to the ContentList, and not the
     * flexible implementation used by AbstractList. It needs to be fast because
     * iterator() is used extensively in the for-each type loop.
     *
     * @author Rolf Lear
     */
    private inner class CLListIterator
    /**
     * Default constructor
     *
     * @param flist
     *        The FilterList over which we will iterate.
     * @param start
     *        where in the FilterList to start iteration.
     */
    (start: Int) : MutableListIterator<Content> {
        /** Whether this iterator is in forward or reverse.  */
        private var forward = false

        /** Whether a call to remove() is valid  */
        private var canremove = false

        /** Whether a call to set() is valid  */
        private var canset = false

        /** Expected modCount in our backing list  */
        private var expectedmod = -1

        private var cursor = -1

        init {
            expectedmod = getModCount()
            // always start list iterators in backward mode ....
            // it makes sense... really.
            forward = false

            checkIndex(start, false)

            cursor = start
        }

        private fun checkConcurrent() {
            if (expectedmod != getModCount()) {
                throw ConcurrentModificationException(
                    "The ContentList " +
                            "supporting this iterator has been modified by" +
                            "something other than this Iterator."
                )
            }
        }

        /**
         * Returns `true` if this list iterator has a next element.
         */
        override fun hasNext(): Boolean {
            return (if (forward) cursor + 1 else cursor) < count
        }

        /**
         * Returns `true` if this list iterator has more elements
         * when traversing the list in the reverse direction.
         */
        override fun hasPrevious(): Boolean {
            return (if (forward) cursor else cursor - 1) >= 0
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to `next`.
         */
        override fun nextIndex(): Int {
            return if (forward) cursor + 1 else cursor
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to `previous`. (Returns -1 if the list
         * iterator is at the beginning of the list.)
         */
        override fun previousIndex(): Int {
            return if (forward) cursor else cursor - 1
        }

        /**
         * Returns the next element in the list.
         */
        override fun next(): Content {
            checkConcurrent()
            val next = if (forward) cursor + 1 else cursor

            if (next >= count) {
                throw NoSuchElementException("next() is beyond the end of the Iterator")
            }

            cursor = next
            forward = true
            canremove = true
            canset = true
            return elementData!![cursor]!!
        }

        /**
         * Returns the previous element in the list.
         */
        override fun previous(): Content {
            checkConcurrent()
            val prev = if (forward) cursor else cursor - 1

            if (prev < 0) {
                throw NoSuchElementException("previous() is beyond the beginning of the Iterator")
            }

            cursor = prev
            forward = false
            canremove = true
            canset = true
            return elementData!![cursor]!!
        }

        /**
         * Inserts the specified element into the list .
         */
        override fun add(element: Content) {
            checkConcurrent()
            // always add before what would normally be returned by next();
            val next = if (forward) cursor + 1 else cursor

            this@ContentList.add(next, element)

            expectedmod = getModCount()

            canset = false
            canremove = canset

            // a call to next() should be unaffected, so, whatever was going to
            // be next will still be next, remember, what was going to be next
            // has been shifted 'right' by our insert.
            // we ensure this by setting the cursor to next(), and making it
            // forward
            cursor = next
            forward = true
        }

        /**
         * Removes from the list the last element that was returned by the last
         * call to `next` or `previous`.
         */
        override fun remove() {
            checkConcurrent()
            if (!canremove)
                throw IllegalStateException(
                    "Can not remove an "
                            + "element unless either next() or previous() has been called "
                            + "since the last remove()"
                )
            // we are removing the last entry returned by either next() or
            // previous().
            // the idea is to remove it, and pretend that we used to be at the
            // entry that happened *after* the removed entry.
            // so, get what would be the next entry (set at tmpcursor).
            // so call nextIndex to set tmpcursor to what would come after.
            this@ContentList.removeAt(cursor)
            forward = false
            expectedmod = getModCount()

            canremove = false
            canset = false
        }

        /**
         * Replaces the last element returned by `next` or
         * `previous` with the specified element.
         */
        override fun set(element: Content) {
            checkConcurrent()
            if (!canset) {
                throw IllegalStateException(
                    "Can not set an element "
                            + "unless either next() or previous() has been called since the "
                            + "last remove() or set()"
                )
            }

            this@ContentList.set(cursor, element)
            expectedmod = getModCount()

        }

    }

    /* * * * * * * * * * * * FilterList * * * * * * * * * * * * * * * */
    /* * * * * * * * * * * * * FilterList * * * * * * * * * * * * * * * */

    /**
     * `FilterList` represents legal JDOM content, including content
     * for `Document`s or `Element`s.
     *
     * FilterList represents a dynamic view of the backing ContentList, changes
     * to the backing list are reflected in the FilterList, and visa-versa.
     *
     * @param <F>
     *        The Generic type of content accepted by the underlying Filter.
     */
    inner class FilterList<F : Content>
    /**
     * Create a new instance of the FilterList with the specified Filter.
     *
     * @param filter
     *        The underlying Filter to use for filtering the content.
     */
    internal constructor(
        // The filter to apply
        @JvmField internal val filter: Filter<F>
    ) : AbstractMutableList<F>() {

        // correlate the position in the filtered list to the index in the
        // backing ContentList.
        @JvmField
        internal var backingpos: IntArray = IntArray(count + INITIAL_ARRAY_SIZE)

        @JvmField
        internal var backingsize = 0

        // track data modifications in the backing ContentList.
        @JvmField
        internal var xdata = -1

        /**
         * Returns true if there is no content in this FilterList.
         * @return true if there is no content in this FilterList
         */
        override fun isEmpty(): Boolean {
            // More efficient implementation than default size() == 0
            // we use resync() to accomplish the task. If there is an
            // element 0 in this FilterList, then it is not empty!
            // we may already have resync'd 0, which will be a fast return then,
            // or, if we have not resync'd 0, then we only have to filter up to
            // the first matching element to get a result (or the whole list
            // if isEmpty() is true).
            return resync(0) == count
        }

        /**
         * Synchronise our view to the backing list. Only synchronise the first
         * `index` view elements. For want of a better word, we'll
         * call this a 'Lazy' implementation.
         *
         * @param index
         *        how much we want to sync. Set to -1 to synchronise everything.
         * @return the index in the backing array of the *index'th* match.
         *         or the backing data size if there is no match for the index.
         */
        internal fun resync(index: Int): Int {
            if (xdata != getDataModCount()) {
                // The underlying list was modified somehow...
                // we need to invalidate our research...
                xdata = getDataModCount()
                backingsize = 0
                if (count >= backingpos.size) {
                    backingpos = IntArray(count + 1)
                }
            }

            if (index >= 0 && index < backingsize) {
                // we have already indexed far enough...
                // return the backing index.
                return backingpos[index]
            }

            // the index in the backing list of the next value to check.
            var bpi = 0
            if (backingsize > 0) {
                bpi = backingpos[backingsize - 1] + 1
            }

            while (bpi < count) {
                val gotit: F? = filter.filter(elementData!![bpi])
                if (gotit != null) {
                    backingpos[backingsize] = bpi
                    if (backingsize++ == index) {
                        return bpi
                    }
                }
                bpi++
            }
            return count
        }

        /**
         * Inserts the specified object at the specified position in this list.
         * Shifts the object currently at that position (if any) and any
         * subsequent objects to the right (adds one to their indices).
         *
         * @param index
         *        The location to set the value to.
         * @param element
         *        The object to insert into the list. throws
         *        IndexOutOfBoundsException if index < 0 || index > size()
         */
        override fun add(index: Int, element: F) {
            val obj: Content = element
            if (index < 0) {
                throw IndexOutOfBoundsException("Index: " + index + " Size: " + size)
            }
            val adj = resync(index)
            if (adj == count && index > size) {
                throw IndexOutOfBoundsException("Index: " + index + " Size: " + size)
            }
            if (filter.matches(obj)) {
                this@ContentList.add(adj, obj)

                // we can optimise the laziness now by doing a partial reset on
                // the backing list... invalidate everything *after* the added
                // content
                if (backingpos.size <= count) {
                    backingpos = ArrayCopy.copyOf(backingpos, backingpos.size + 1)
                }
                backingpos[index] = adj
                backingsize = index + 1
                xdata = getDataModCount()

            } else {
                throw IllegalAddException(
                    "Filter won't allow the " +
                            obj.javaClass.name +
                            " '" + obj + "' to be added to the list"
                )
            }
        }

        override fun addAll(
            index: Int,
            elements: Collection<F>
        ): Boolean {
            val collection: Collection<F>? = elements
            if (collection == null) {
                throw NullPointerException("Cannot add a null collection")
            }

            if (index < 0) {
                throw IndexOutOfBoundsException("Index: " + index + " Size: " + size)
            }

            val adj = resync(index)
            if (adj == count && index > size) {
                throw IndexOutOfBoundsException("Index: " + index + " Size: " + size)
            }

            val addcnt = collection.size
            if (addcnt == 0) {
                return false
            }

            this@ContentList.ensureCapacity(this@ContentList.size + addcnt)

            val tmpmodcount = getModCount()
            val tmpdmc = getDataModCount()
            var ok = false

            var cnt = 0

            try {
                for (c0 in collection) {
                    val c: Content? = c0
                    if (c == null) {
                        throw NullPointerException(
                            "Cannot add null content"
                        )
                    }
                    if (filter.matches(c)) {
                        this@ContentList.add(adj + cnt, c)
                        // we can optimise the laziness now by doing a partial
                        // reset on
                        // the backing list... invalidate everything *after* the
                        // added
                        // content
                        if (backingpos.size <= count) {
                            backingpos = ArrayCopy.copyOf(backingpos, backingpos.size + addcnt)
                        }
                        backingpos[index + cnt] = adj + cnt
                        backingsize = index + cnt + 1
                        xdata = getDataModCount()

                        cnt++
                    } else {
                        throw IllegalAddException(
                            "Filter won't allow the " +
                                    c.javaClass.name +
                                    " '" + c + "' to be added to the list"
                        )

                    }
                }
                ok = true
            } finally {
                if (!ok) {
                    // something failed... remove all added content
                    while (--cnt >= 0) {
                        this@ContentList.removeAt(adj + cnt)
                    }
                    // restore the mod-counts.
                    setModCount(tmpmodcount, tmpdmc)
                    // reset the cache... will need to redo some work on another
                    // call maybe....
                    backingsize = index
                    xdata = tmpmodcount
                }
            }

            return true
        }

        /**
         * Return the object at the specified offset.
         *
         * @param index
         *        The offset of the object.
         * @return The Object which was returned.
         */
        override fun get(index: Int): F {
            if (index < 0) {
                throw IndexOutOfBoundsException("Index: " + index + " Size: " + size)
            }
            val adj = resync(index)
            if (adj == count) {
                throw IndexOutOfBoundsException("Index: " + index + " Size: " + size)
            }
            return filter.filter(this@ContentList.get(adj))!!
        }

        override fun iterator(): MutableIterator<F> {
            return FilterListIterator<F>(this, 0)
        }

        override fun listIterator(): MutableListIterator<F> {
            return FilterListIterator<F>(this, 0)
        }

        override fun listIterator(index: Int): MutableListIterator<F> {
            return FilterListIterator<F>(this, index)
        }

        /**
         * Remove the object at the specified offset.
         *
         * @param index
         *        The offset of the object.
         * @return The Object which was removed.
         */
        override fun removeAt(index: Int): F {
            if (index < 0) {
                throw IndexOutOfBoundsException("Index: " + index + " Size: " + size)
            }
            val adj = resync(index)
            if (adj == count) {
                throw IndexOutOfBoundsException("Index: " + index + " Size: " + size)
            }
            val oldc = this@ContentList.removeAt(adj)
            // optimise the backing cache.
            backingsize = index
            xdata = getDataModCount()
            // use Filter to ensure the cast is right.
            return filter.filter(oldc)!!
        }

        /**
         * Set the object at the specified location to the supplied object.
         *
         * @param index
         *        The location to set the value to.
         * @param element
         *        The location to set the value to.
         * @return The object which was replaced. throws
         *         IndexOutOfBoundsException if index < 0 || index >= size()
         */
        override fun set(index: Int, element: F): F {
            val obj = element
            if (index < 0) {
                throw IndexOutOfBoundsException("Index: " + index + " Size: " + size)
            }
            val adj = resync(index)
            if (adj == count) {
                throw IndexOutOfBoundsException("Index: " + index + " Size: " + size)
            }
            val ins: F? = filter.filter(obj)
            if (ins != null) {
                val oldc: F = filter.filter(this@ContentList.set(adj, ins))!!
                // optimize the backing....
                xdata = getDataModCount()
                return oldc
            }
            throw IllegalAddException(
                "Filter won't allow index " +
                        index + " to be set to " +
                        (obj.javaClass).name
            )
        }

        /**
         * Return the number of items in this list
         *
         * @return The number of items in this list.
         */
        override val size: Int
            get() {
                resync(-1)
                return backingsize
            }

        /**
         * Unlike the Arrays.binarySearch, this method never expects an
         * "already exists" condition, we only ever add, thus there will never
         * be a negative insertion-point.
         * @param indexes THe pointers to search within
         * @param len The number of pointers to search within
         * @param `val` The pointer we are checking for.
         * @param comp The Comparator to compare with
         * @return the insertion point.
         */
        @Suppress("UNCHECKED_CAST")
        private fun fbinarySearch(
            indexes: IntArray, len: Int,
            `val`: Int, comp: Comparator<in F>
        ): Int {
            var left = 0
            var mid = 0
            var right = len - 1
            var cmp = 0
            val base = elementData!![backingpos[`val`]] as F
            while (left <= right) {
                mid = (left + right) ushr 1
                cmp = comp.compare(base, elementData!![indexes[mid]] as F)
                if (cmp == 0) {
                    while (cmp == 0 && mid < right && comp.compare(
                            base, elementData!![indexes[mid + 1]] as F
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


        /**
         * Sorts this list using the supplied Comparator to compare elements.
         *
         * @param comp - the Comparator used to compare list elements. A null value indicates that the elements' natural ordering should be used
         * @Since 2.0.6
         */
        //Not till Java8 @Override
        override fun sort(comp: Comparator<in F>?) {
            sortContent(comp)
        }

        /** The body of [sort], reachable from Kotlin callers (see [ContentList.sortContent]). */
        fun sortContent(comp: Comparator<in F>?) {
            // this size() forces a full scan/update of the list.
            if (comp == null) {
                // sort by the 'natural order', which, there is none.
                // options, throw exception, or let the current-order represent the natural order.
                // do nothing is the better alternative.
                return
            }
            val sz = size
            val indexes = IntArray(sz)
            for (i in 0 until sz) {
                val ip = fbinarySearch(indexes, i, i, comp)
                if (ip < i) {
                    System.arraycopy(indexes, ip, indexes, ip + 1, i - ip)
                }
                indexes[ip] = backingpos[i]
            }
            sortInPlace(indexes)
        }

    }

    /* * * * * * * * * * * * * FilterListIterator * * * * * * * * * * * */
    /* * * * * * * * * * * * * FilterListIterator * * * * * * * * * * * */

    inner class FilterListIterator<F : Content>
    /**
     * Default constructor
     *
     * @param flist
     *        The FilterList over which we will iterate.
     * @param start
     *        where in the FilterList to start iteration.
     */
    internal constructor(
        /** The Filter that applies  */
        private val filterlist: FilterList<F>,
        start: Int
    ) : MutableListIterator<F> {

        /** Whether this iterator is in forward or reverse.  */
        private var forward = false

        /** Whether a call to remove() is valid  */
        private var canremove = false

        /** Whether a call to set() is valid  */
        private var canset = false

        /** Expected modCount in our backing list  */
        private var expectedmod = -1

        private var cursor = -1

        init {
            expectedmod = getModCount()
            // always start list iterators in backward mode ....
            // it makes sense... really.
            forward = false

            if (start < 0) {
                throw IndexOutOfBoundsException("Index: " + start + " Size: " + filterlist.size)
            }

            val adj = filterlist.resync(start)

            if (adj == count && start > filterlist.size) {
                // the start point is after the end of the list.
                // it is only allowed to be the same as size(), no larger.
                throw IndexOutOfBoundsException("Index: " + start + " Size: " + filterlist.size)
            }

            cursor = start
        }

        private fun checkConcurrent() {
            if (expectedmod != getModCount()) {
                throw ConcurrentModificationException(
                    "The ContentList " +
                            "supporting the FilterList this iterator is " +
                            "processing has been modified by something other " +
                            "than this Iterator."
                )
            }
        }

        /**
         * Returns `true` if this list iterator has a next element.
         */
        override fun hasNext(): Boolean {
            return filterlist.resync(if (forward) cursor + 1 else cursor) < count
        }

        /**
         * Returns `true` if this list iterator has more elements
         * when traversing the list in the reverse direction.
         */
        override fun hasPrevious(): Boolean {
            return (if (forward) cursor else cursor - 1) >= 0
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to `next`.
         */
        override fun nextIndex(): Int {
            return if (forward) cursor + 1 else cursor
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to `previous`. (Returns -1 if the list
         * iterator is at the beginning of the list.)
         */
        override fun previousIndex(): Int {
            return if (forward) cursor else cursor - 1
        }

        /**
         * Returns the next element in the list.
         */
        override fun next(): F {
            checkConcurrent()
            val next = if (forward) cursor + 1 else cursor

            if (filterlist.resync(next) >= count) {
                throw NoSuchElementException("next() is beyond the end of the Iterator")
            }

            cursor = next
            forward = true
            canremove = true
            canset = true
            return filterlist.get(cursor)
        }

        /**
         * Returns the previous element in the list.
         */
        override fun previous(): F {
            checkConcurrent()
            val prev = if (forward) cursor else cursor - 1

            if (prev < 0) {
                throw NoSuchElementException("previous() is beyond the beginning of the Iterator")
            }

            cursor = prev
            forward = false
            canremove = true
            canset = true
            return filterlist.get(cursor)
        }

        /**
         * Inserts the specified element into the list .
         */
        override fun add(element: F) {
            checkConcurrent()
            // always add before what would normally be returned by next();
            val next = if (forward) cursor + 1 else cursor

            filterlist.add(next, element)

            expectedmod = getModCount()

            canset = false
            canremove = canset

            // a call to next() should be unaffected, so, whatever was going to
            // be next will still be next, remember, what was going to be next
            // has been shifted 'right' by our insert.
            // we ensure this by setting the cursor to next(), and making it
            // forward
            cursor = next
            forward = true
        }

        /**
         * Removes from the list the last element that was returned by the last
         * call to `next` or `previous`.
         */
        override fun remove() {
            checkConcurrent()
            if (!canremove)
                throw IllegalStateException(
                    "Can not remove an "
                            + "element unless either next() or previous() has been called "
                            + "since the last remove()"
                )
            // we are removing the last entry returned by either next() or
            // previous().
            // the idea is to remove it, and pretend that we used to be at the
            // entry that happened *after* the removed entry.
            // so, get what would be the next entry (set at tmpcursor).
            // so call nextIndex to set tmpcursor to what would come after.
            filterlist.removeAt(cursor)
            forward = false
            expectedmod = getModCount()

            canremove = false
            canset = false
        }

        /**
         * Replaces the last element returned by `next` or
         * `previous` with the specified element.
         */
        override fun set(element: F) {
            checkConcurrent()
            if (!canset) {
                throw IllegalStateException(
                    "Can not set an element "
                            + "unless either next() or previous() has been called since the "
                            + "last remove() or set()"
                )
            }

            filterlist.set(cursor, element)
            expectedmod = getModCount()

        }

    }

    private companion object {
        private const val INITIAL_ARRAY_SIZE = 4

        /** Remove the parent of a Object  */
        private fun removeParent(c: Content) {
            c.setParent(null)
        }
    }

}

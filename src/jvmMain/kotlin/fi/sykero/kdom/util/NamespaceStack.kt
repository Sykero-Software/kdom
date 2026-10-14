/*--

 Copyright (C) 2011 - 2012 Jason Hunter & Brett McLaughlin.
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

package fi.sykero.kdom.util

import fi.sykero.kdom.Attribute
import fi.sykero.kdom.Element
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.internal.ArrayCopy

/**
 * A high-performance stack for processing those Namespaces that are introduced
 * or are in-scope at a point in an Element hierarchy.
 * <p>
 * This stack implements the 'Namespace Rules' which XML uses, where a Namespace
 * 'redefines' an existing Namespace if they share the same prefix. This class
 * is intended to provide a high-performance mechanism for calculating the
 * Namespace scope for an Element, and identifying what Namespaces an Element
 * introduces in to the scope. This is not a validation tool.
 * <p>
 * This class implements Iterable which means it can be used in the context
 * of a for-each type loop:
 * <br>
 * <code><pre>
 *   NamespaceStack namespacestack = new NamespaceStack();
 *   for (Namespace ns : namespacestack) {
 *      ...
 *   }
 * </pre></code>
 * The Iteration in the above example will return those Namespaces which are
 * in-scope for the current level of the stack. The Namespace order will follow
 * the JDOM 'standard'. The first namespace will be the Element's Namespace. The
 * subsequent Namespaces will be the other in-scope namespaces in alphabetical
 * order by the Namespace prefix.
 * <p>
 * NamespaceStack does not validate the push()/pop() cycles. It does not ensure
 * that the pop() is for the same element that was previously pushed. Further,
 * it does not check to make sure that the pushed() Element is the natural child
 * of the previously pushed() Element.
 * 
 * @author Rolf Lear
 *
 */
class NamespaceStack(seed: Array<Namespace>) : Iterable<Namespace> {

    private class ForwardWalker(private val namespaces: Array<Namespace>) : Iterator<Namespace> {
        private var cursor = 0

        override fun hasNext(): Boolean {
            return cursor < namespaces.size
        }

        override fun next(): Namespace {
            if (cursor >= namespaces.size) {
                throw NoSuchElementException("Cannot over-iterate...")
            }
            return namespaces[cursor++]
        }

        fun remove() {
            throw UnsupportedOperationException(
                "Cannot remove Namespaces from iterator"
            )
        }
    }

    private class BackwardWalker(private val namespaces: Array<Namespace>) : Iterator<Namespace> {
        private var cursor = namespaces.size - 1

        override fun hasNext(): Boolean {
            return cursor >= 0
        }

        override fun next(): Namespace {
            if (cursor < 0) {
                throw NoSuchElementException("Cannot over-iterate...")
            }
            return namespaces[cursor--]
        }

        fun remove() {
            throw UnsupportedOperationException(
                "Cannot remove Namespaces from iterator"
            )
        }
    }

    private class NamespaceIterable(
        private val namespaces: Array<Namespace>,
        private val forward: Boolean
    ) : Iterable<Namespace> {
        override fun iterator(): Iterator<Namespace> {
            return if (forward) ForwardWalker(namespaces) else BackwardWalker(namespaces)
        }
    }

    /**
     * Convenience class that makes very fast work for an empty Namespace array.
     * It doubles up as both Iterator and Interable.
     * @author rolf
     */
    private class EmptyIterable : Iterable<Namespace>, Iterator<Namespace> {
        override fun iterator(): Iterator<Namespace> {
            return this
        }

        override fun hasNext(): Boolean {
            return false
        }

        override fun next(): Namespace {
            throw NoSuchElementException(
                "Can not call next() on an empty Iterator."
            )
        }

        fun remove() {
            throw UnsupportedOperationException(
                "Cannot remove Namespaces from iterator"
            )
        }
    }

    /** The namespaces added to the scope at each depth */
    private var added: Array<Array<Namespace>?> = arrayOfNulls<Array<Namespace>>(10)

    /** The entire scope at each depth */
    private var scopes: Array<Array<Namespace>?> = arrayOfNulls<Array<Namespace>>(10)

    /** The current depth */
    private var depth = -1

    /**
     * Create a NamespaceWalker ready to use as a stack.
     * <br>
     * @see push (Element) for comprehensive notes.
     */
    constructor() : this(DEFAULTSEED)

    init {
        depth++
        added[depth] = seed

        scopes[depth] = added[depth]
    }

    /**
     * Create a new in-scope level for the Stack based on an Element.
     *
     * The Namespaces associated with the input Element are used to modify the
     * 'in-scope' Namespaces in this NamespaceStack.
     *
     * The following 'rules' will be applied:
     *
     * - Namespaces used in the input Element that were not part of the previous
     *   scope will be added to the new scope level in the stack.
     * - If a new Namespace is added to the scope, but the previous scope
     *   already had a namespace with the same prefix, then that previous
     *   namespace is removed from the new scope (the new Namespace replaces
     *   the previous namespace with the same prefix).
     * - The order of the in-scope Namespaces will always be: first the
     *   Namespace of the input Element followed by all other in-scope
     *   Namespaces sorted alphabetically by prefix.
     * - The new in-scope Namespace values will be available in this class's
     *   iterator() method (which is available as part of this class's
     *   *Iterable* implementation.
     * - The namespaces added to the scope by the input Element will be
     *   available in the [addedForward] Iterable. The order of
     *   the added Namespaces follows the same rules as above: first the
     *   Element Namespace (only if that Namespace is actually added) followed
     *   by the other added namespaces in alphabetical-by-prefix order.
     * - The same added namespaces are also available in reverse order in
     *   the [addedReverse] Iterable.
     *
     * @param element The element at the new level of the stack.
     */
    fun push(element: Element) {

        // how many times do you add more than 8 namespaces in one go...
        // we can add more if we need to...
        val toadd = ArrayList<Namespace>(8)
        val mns = element.namespace
        // check to see whether the Namespace is new-to-scope.
        var newscope = checkNamespace(toadd, mns, scopes[depth]!!)
        if (element.hasAdditionalNamespaces()) {
            for (ns in element.additionalNamespaces) {
                if (ns === mns) {
                    continue
                }
                // check to see whether the Namespace is new-to-scope.
                newscope = checkNamespace(toadd, ns, newscope)
            }
        }
        if (element.hasAttributes()) {
            for (a in element.attributes) {
                val ns = a.namespace
                if (ns === Namespace.NO_NAMESPACE) {
                    // Attributes are allowed to be in the NO_NAMESPACE without
                    // changing the in-scope set of the Element.... special-case
                    continue
                }
                if (ns === mns) {
                    continue
                }
                // check to see whether the Namespace is new-to-scope.
                newscope = checkNamespace(toadd, ns, newscope)
            }
        }

        pushStack(mns, newscope, toadd)
    }

    /**
     * Create a new in-scope level for the Stack based on an Attribute.
     *
     * @param att The attribute to contribute to the namespace scope.
     */
    fun push(att: Attribute) {
        val toadd = ArrayList<Namespace>(1)
        val mns = att.namespace
        // check to see whether the Namespace is new-to-scope.
        val newscope = checkNamespace(toadd, mns, scopes[depth]!!)

        pushStack(mns, newscope, toadd)
    }

    /**
     * Create a new in-scope level for the Stack based on an arbitrary set of Namespaces.
     * @param namespaces The Iterable format for the Namespaces.
     */
    fun push(namespaces: Iterable<Namespace>) {

        // how many times do you add more than 8 namespaces in one go...
        // we can add more if we need to...
        val toadd = ArrayList<Namespace>(8)
        var newscope = scopes[depth]!!
        for (ns in namespaces) {
            // check to see whether the Namespace is new-to-scope.
            newscope = checkNamespace(toadd, ns, newscope)
        }

        pushStack(Namespace.XML_NAMESPACE, newscope, toadd)
    }

    /**
     * Create a new in-scope level for the Stack based on an arbitrary set of Namespaces.
     * The first Namespace in the list will be considered the 'primary' namespace for this scope
     * and will be sorted to the front. If no namespaces are supplied then the 'current' scope will
     * be duplicated (including sort order) as the new scope.
     * @param namespaces The array of Namespaces.
     */
    fun push(vararg namespaces: Namespace) {

        if (namespaces.size == 0) {
            // duplicate the current level to the new one.
            pushStack(scopes[depth]!![0], scopes[depth]!!, EMPTYLIST)
            return
        }

        // how many times do you add more than 8 namespaces in one go...
        // we can add more if we need to...
        val toadd = ArrayList<Namespace>(8)
        var newscope = scopes[depth]!!
        for (ns in namespaces) {
            // check to see whether the Namespace is new-to-scope.
            newscope = checkNamespace(toadd, ns, newscope)
        }

        pushStack(namespaces[0], newscope, toadd)
    }

    private fun pushStack(mns: Namespace, newscope: Array<Namespace>, toadd: List<Namespace>) {
        var newscope = newscope
        // OK, we've checked the namespaces in the Element, and 'toadd' contains
        // all namespaces that are not already in scope.
        depth++

        if (depth >= scopes.size) {
            // we need more space on the stack.
            scopes = ArrayCopy.copyOf(scopes, scopes.size * 2)
            added = ArrayCopy.copyOf(added, scopes.size)
        }

        // Sort out the added namespaces.
        if (toadd.isEmpty()) {
            // nothing changed in the scope.
            added[depth] = EMPTY
        } else {
            val addedNow = toadd.toTypedArray()
            added[depth] = addedNow
            if (addedNow[0] === mns) {
                addedNow.sortWith(NSCOMP, 1, addedNow.size)
            } else {
                addedNow.sortWith(NSCOMP)
            }
        }

        if (mns !== newscope[0]) {
            if (toadd.isEmpty()) {
                // we need to make newscope a copy of the previous level's
                // scope, because it is not yet a copy.
                newscope = ArrayCopy.copyOf(newscope, newscope.size)
            }
            // we need to take the Namespace at position 0, and insert it
            // in its place later in the array.
            // we need to take the mns from later in the array, and move it
            // to the front.
            val tmp = newscope[0]
            var ip = -binarySearch(newscope, 1, newscope.size, tmp) - 1
            // we can be sure that (- ip - 1 ) is >= 1
            // we also know that we want to move the data before the ip
            // backwards one spot, so the math is slightly different....
            ip--
            newscope.copyInto(newscope, 0, 1, 1 + ip)
            newscope[ip] = tmp

            ip = binarySearch(newscope, 0, newscope.size, mns)
            // we can be sure that ip is >= 0
            newscope.copyInto(newscope, 1, 0, ip)
            newscope[0] = mns
        }

        scopes[depth] = newscope
    }

    /**
     * Restore stack to the level prior to the current one. The various Iterator
     * methods will thus return the data at the previous level.
     */
    fun pop() {
        if (depth <= 0) {
            throw IllegalStateException("Cannot over-pop the stack.")
        }
        scopes[depth] = null
        added[depth] = null
        depth--
    }

    /**
     * Return an Iterable containing all the Namespaces introduced to the
     * current-level's scope.
     * @see push (Element) for the details on the data order.
     * @return A read-only Iterable containing added Namespaces (may be empty);
     */
    fun addedForward(): Iterable<Namespace> {
        if (added[depth]!!.size == 0) {
            return EMPTYITER
        }
        return NamespaceIterable(added[depth]!!, true)
    }

    /**
     * Return an Iterable containing all the Namespaces introduced to the
     * current-level's scope but in reverse order to [addedForward].
     * @see push (Element) for the details on the data order.
     * @return A read-only Iterable containing added Namespaces (may be empty);
     */
    fun addedReverse(): Iterable<Namespace> {
        if (added[depth]!!.size == 0) {
            return EMPTYITER
        }
        return NamespaceIterable(added[depth]!!, false)
    }

    /**
     * Get all the Namespaces in-scope at the current level of the stack.
     * @see push (Element) for the details on the data order.
     * @return A read-only Iterator containing added Namespaces (may be empty);
     */
    override fun iterator(): Iterator<Namespace> {
        return ForwardWalker(scopes[depth]!!)
    }

    /**
     * Return a new array instance representing the current scope.
     * Modifying the returned array will not affect this scope.
     * @return a copy of the current scope.
     */
    val scope: Array<Namespace>
        get() = ArrayCopy.copyOf(scopes[depth]!!, scopes[depth]!!.size)

    /**
     * Inspect the current scope and return true if the specified namespace is
     * in scope.
     * @param ns The Namespace to check
     * @return true if the current scope contains that Namespace.
     */
    fun isInScope(ns: Namespace): Boolean {
        if (ns === scopes[depth]!![0]) {
            return true
        }
        val ip = binarySearch(scopes[depth]!!, 1, scopes[depth]!!.size, ns)
        if (ip >= 0) {
            // we have the same prefix.
            return ns === scopes[depth]!![ip]
        }
        return false
    }

    /**
     * Get the Namespace in the current scope with the specified prefix.
     * @param prefix The prefix to get the namespace for (null is treated the same as "").
     * @return The Namespace with the specified prefix, or null if the prefix is not in scope.
     * @since JDOM 2.1.0
     */
    fun getNamespaceForPrefix(prefix: String?): Namespace? {
        if (prefix == null) {
            return getNamespaceForPrefix("")
        }
        val nsa = scopes[depth]!!
        for (ns in nsa) {
            if (prefix == ns.prefix) {
                return ns
            }
        }
        return null
    }

    /**
     * Get the **first** Namespace in the current scope that is bound to the specified URI.
     * @param uri The URI to get the first prefix for (null is treated the same as "").
     * @return The first bound Namespace for the specified URI, or null if the URI is not bound.
     * @since JDOM 2.1.0
     */
    fun getFirstNamespaceForURI(uri: String?): Namespace? {
        if (uri == null) {
            return getFirstNamespaceForURI("")
        }
        for (ns in scopes[depth]!!) {
            if (uri == ns.uri) {
                return ns
            }
        }
        return null
    }

    /**
     * Get all prefixes in the current scope that are bound to the specified URI.
     * @param uri The URI to get the first prefix for (null is treated the same as "").
     * @return All bound prefixes for the specified URI, or an empty array if the URI is not bound.
     * @since JDOM 2.1.0
     */
    fun getAllNamespacesForURI(uri: String?): Array<Namespace> {
        if (uri == null) {
            return getAllNamespacesForURI("")
        }
        val al = ArrayList<Namespace>(4)
        for (ns in scopes[depth]!!) {
            if (uri == ns.uri) {
                al.add(ns)
            }
        }
        return al.toTypedArray()
    }

    /**
     * If the specified prefix was bound in the previous bind level, and has
     * been rebound to a different URI in the current level, then return the
     * Namespace the the prefix **was bound to** before.
     * @param prefix The prefix to check for re-binding
     * @return the previous binding for the specified prefix, or null if the prefix was
     *         not previously bound, or was not changed in this level of the stack.
     * @since JDOM 2.1.0
     */
    fun getRebound(prefix: String?): Namespace? {
        if (depth <= 0) {
            return null
        }
        for (nsa in added[depth]!!) {
            if (nsa.prefix == prefix) {
                for (nsp in scopes[depth - 1]!!) {
                    if (nsp.prefix == prefix) {
                        return nsp
                    }
                }
                return null
            }
        }
        return null
    }

    private companion object {
        /** A simple empty Namespace Array to avoid redundant empty instances */
        private val EMPTY: Array<Namespace> = arrayOf()

        private val EMPTYLIST: List<Namespace> = emptyList()

        /** A simple Iterable instance that is always empty. Saves some memory */
        private val EMPTYITER: Iterable<Namespace> = EmptyIterable()

        /** A comparator that sorts Namespaces by their prefix. */
        private val NSCOMP: Comparator<Namespace> = Comparator<Namespace> { ns1, ns2 ->
            ns1.prefix.compareTo(ns2.prefix)
        }

        private val DEFAULTSEED: Array<Namespace> = arrayOf(
            Namespace.NO_NAMESPACE, Namespace.XML_NAMESPACE
        )

        /**
         * Lots of reasons for having our own binarySearch (specific for Namespaces, using == search;
         * see the original JDOM notes on the IBM J9 AIX bug, which we never encounter because the
         * scope array always has more than one member).
         * @param data The Namespaces to search.
         * @param left The left side of the range to search **INCLUSIVE**
         * @param right The right side of the range to search **EXCLUSIVE**
         * @param key The Namespace to search for.
         * @return the 'insertion point' - This return value follows the same convention
         *      as the standard Java BinarySearch methods (see the JavaDoc for Arrays.binarySearch().
         *      In summary, if the value exists then the return value is the index of the existing value.
         *      If the value was not found, then the return value will be negative, and the
         *      place where the missing value should be inserted, can be determined by
         *      adding 1, and converting back to positive (or converting to positive, and subtracting 1).
         */
        private fun binarySearch(data: Array<Namespace>, left: Int, right: Int, key: Namespace): Int {
            var left = left
            var right = right
            // assume all input is valid. No need to waste time checking.

            //		Because we are always searching inside of the scope array, and
            //		because there's always at least two scope members, we will always have
            //		a minimum value of 2 for 'right', and a maximum value of 1 for 'left'
            //		thus the following check is never needed.
            //
            //		if (left >= right) {
            //			// we are searching in nothing, return the correct value
            //			// ... this is where IBM's JDK is broken - it just returns -1
            //			return -left - 1;
            //		}
            // make the right-side 'inclusive' instead of 'exclusive'
            right--

            while (left <= right) {
                // get the mid-point. See the notes on the binary-search bug...
                // ... not that we'll ever have that many Namspaces.... ;-)
                // http://googleresearch.blogspot.com/2006/06/extra-extra-read-all-about-it-nearly.html
                val mid = (left + right) ushr 1
                if (data[mid] === key) {
                    // exact namespace match.
                    return mid
                }
                val cmp = NSCOMP.compare(data[mid], key)

                if (cmp < 0) {
                    left = mid + 1
                } else if (cmp > 0) {
                    right = mid - 1
                } else {
                    // Namespace prefix match.
                    return mid
                }
            }
            return -left - 1
        }

        /**
         * Inspect the *scope* array to see whether the *namespace*
         * Namespace is 'new' or not. If it is 'new' then it is added to the
         * *store* List.
         * @param store Where to add the *namespace* if it is 'new'
         * @param namespace The Namespace to check
         * @param scope The array of Namespaces that are currently in-scope.
         * @return The revised version of 'in-scope' if the scope has changed. If
         *         there is no modification then the same input scope will be returned.
         */
        private fun checkNamespace(
            store: MutableList<Namespace>,
            namespace: Namespace,
            scope: Array<Namespace>
        ): Array<Namespace> {
            // Scope is always sorted as the primary namespace first, then the
            // rest are in prefix order.
            // We can guarantee that the prefixes are all unique too.
            // There is always going to be at least two namespaces in scope with
            // the prefixes : "" and "xml"
            // As a result we can use the 0th index with impunity.
            if (namespace === scope[0]) {
                // we are already in scope.
                return scope
            }
            if (namespace.prefix == scope[0].prefix) {
                // the prefix is the previous scope's primary prefix. This means
                // that we know for sure that the input namespace is new-to-scope.
                store.add(namespace)
                val nscope = ArrayCopy.copyOf(scope, scope.size)
                nscope[0] = namespace
                return nscope
            }
            // will return +ve number if the prefix matches too.
            var ip = binarySearch(scope, 1, scope.size, namespace)
            if (ip >= 0 && namespace === scope[ip]) {
                // the namespace is already in scope.
                return scope
            }
            store.add(namespace)
            if (ip >= 0) {
                // a different namespace with the same prefix as us is in-scope.
                // replace it....
                val nscope = ArrayCopy.copyOf(scope, scope.size)
                nscope[ip] = namespace
                return nscope
            }
            // We are a new prefix in-scope.
            val nscope = ArrayCopy.copyOf(scope, scope.size + 1)
            ip = -ip - 1
            nscope.copyInto(nscope, ip + 1, ip, ip + (nscope.size - ip - 1))
            nscope[ip] = namespace
            return nscope
        }
    }
}

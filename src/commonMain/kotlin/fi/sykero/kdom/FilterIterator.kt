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
import fi.sykero.kdom.util.IteratorIterable

/**
 * Traverse a parent's children that match the supplied filter.
 *
 * @author Bradley S. Huffman
 * @author Rolf Lear
 * @param <T> The Generic type of content returned by this FilterIterator.
 */
internal class FilterIterator<T>(
    private val iterator: DescendantIterator,
    filter: Filter<T>?
) : IteratorIterable<T> {

    private val filter: Filter<T>
    private var nextObject: T? = null
    private var canremove = false

    init {
        // can trust that iterator is not null, but filter may be.
        if (filter == null) {
            throw NullPointerException(
                "Cannot specify a null Filter " +
                        "for a FilterIterator"
            )
        }
        this.filter = filter
    }

    override fun iterator(): Iterator<T> {
        return FilterIterator<T>(iterator.iterator(), filter)
    }

    override fun hasNext(): Boolean {

        canremove = false

        if (nextObject != null) {
            return true
        }

        while (iterator.hasNext()) {
            val obj: Any = iterator.next()
            val f: T? = filter.filter(obj)
            if (f != null) {
                nextObject = f
                return true
            }
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    override fun next(): T {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        val obj = nextObject as T
        nextObject = null
        canremove = true
        return obj
    }

    override fun remove() {
        if (!canremove) {
            throw IllegalStateException(
                "remove() can only be called " +
                        "on the FilterIterator immediately after a successful " +
                        "call to next(). A call to remove() immediately after " +
                        "a call to hasNext() or remove() will also fail."
            )
        }
        canremove = false
        iterator.remove()
    }
}

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

package fi.sykero.kdom.filter

import fi.sykero.kdom.Content
import java.util.ArrayList
import java.util.Collections
import java.util.RandomAccess

/**
 * Partial implementation of [Filter].
 *
 * @author Bradley S. Huffman
 * @author Rolf Lear
 * @param <T> The Generic type of content returned by this Filter
 */
abstract class AbstractFilter<T> : Filter<T> {

    final override fun matches(content: Any?): Boolean {
        return filter(content) != null
    }

    override fun filter(content: List<*>?): List<T> {
        if (content == null) {
            return Collections.emptyList()
        }
        if (content is RandomAccess) {
            val sz = content.size
            val ret = ArrayList<T>(sz)
            for (i in 0 until sz) {
                val c = filter(content[i])
                if (c != null) {
                    ret.add(c)
                }
            }
            if (ret.isEmpty()) {
                return Collections.emptyList()
            }
            return Collections.unmodifiableList(ret)
        }
        val ret = ArrayList<T>(10)
        val it = content.iterator()
        while (it.hasNext()) {
            val c = filter(it.next())
            if (c != null) {
                ret.add(c)
            }
        }
        if (ret.isEmpty()) {
            return Collections.emptyList()
        }
        return Collections.unmodifiableList(ret)
    }

    final override fun negate(): Filter<*> {
        if (this is NegateFilter) {
            return this.baseFilter
        }
        return NegateFilter(this)
    }

    final override fun or(filter: Filter<*>?): Filter<out Content> {
        return OrFilter(this, filter)
    }

    final override fun and(filter: Filter<*>?): Filter<T> {
        return AndFilter<T>(filter, this)
    }

    override fun <R> refine(filter: Filter<R>?): Filter<R> {
        return AndFilter<R>(this, filter)
    }
}

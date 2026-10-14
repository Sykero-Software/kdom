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

package fi.sykero.kdom.internal

/**
 * The copyOf methods on java.util.Arrays are introduced in Java6. Need an
 * alternative to support Java5.
 * 
 * @author Rolf Lear
 *
 */
class ArrayCopy private constructor() {

    companion object {

	/**
	 * Arrays.copyOf(...) is a Java6 thing. This is a replacement.
	 * @param <E> The generic type of the array we are copying.
	 * @param source the source array.
	 * @param len the length of the new array copy.
	 * @return a new array that has the same elements as the source.
	 */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <E> copyOf(source: kotlin.Array<E>, len: Int): kotlin.Array<E> {
            // kdom: Array.copyOf keeps the runtime component type (jdom2 used java.lang.reflect.Array).
            return source.copyOf(len) as kotlin.Array<E>
        }

	/**
	 * Arrays.copyOf(...) is a Java6 thing. This is a replacement.
	 * @param <E> The generic type of the array we are copying.
	 * @param source the source array.
	 * @param from the start point of the copy (inclusive).
	 * @param to the end point of the copy (exclusive).
	 * @return a new array that has the same elements as the source.
	 */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <E> copyOfRange(source: kotlin.Array<E>, from: Int, to: Int): kotlin.Array<E> {
            val len = to - from
            if (len < 0) {
                throw IllegalArgumentException("From($from) > To ($to)")
            }
            // kdom: Array.copyOf keeps the runtime component type (jdom2 used java.lang.reflect.Array).
            val dest = source.copyOf(len)
            dest.fill(null)
            val tocopy = if (from + len > source.size) source.size - from else len
            source.copyInto(dest, 0, from, from + tocopy)
            return dest as kotlin.Array<E>
        }

	/**
	 * Arrays.copyOf(...) is a Java6 thing. This is a replacement.
	 * @param source the source array.
	 * @param len the length of the new array copy.
	 * @return a new array that has the same elements as the source.
	 */
        @JvmStatic
        fun copyOf(source: CharArray, len: Int): CharArray {
            val dest = CharArray(len)
            source.copyInto(dest, 0, 0, (if (len < source.size) len else source.size))
            return dest
        }

	/**
	 * Arrays.copyOf(...) is a Java6 thing. This is a replacement.
	 * @param source the source array.
	 * @param len the length of the new array copy.
	 * @return a new array that has the same elements as the source.
	 */
        @JvmStatic
        fun copyOf(source: IntArray, len: Int): IntArray {
            val dest = IntArray(len)
            source.copyInto(dest, 0, 0, (if (len < source.size) len else source.size))
            return dest
        }

	/**
	 * Arrays.copyOf(...) is a Java6 thing. This is a replacement.
	 * @param source the source array.
	 * @param len the length of the new array copy.
	 * @return a new array that has the same elements as the source.
	 */
        @JvmStatic
        fun copyOf(source: BooleanArray, len: Int): BooleanArray {
            val dest = BooleanArray(len)
            source.copyInto(dest, 0, 0, (if (len < source.size) len else source.size))
            return dest
        }
    }
}

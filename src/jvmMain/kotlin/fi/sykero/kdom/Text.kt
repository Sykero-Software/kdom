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

import fi.sykero.kdom.output.Format

/**
 * An XML character sequence. Provides a modular, parentable method of
 * representing text. Text makes no guarantees about the underlying textual
 * representation of character data, but does expose that data as a Java String.
 *
 * @author  Brett McLaughlin
 * @author  Jason Hunter
 * @author  Bradley S. Huffman
 */
open class Text : Content {

    /**
     * The actual character content.
     * This is also the XPath 1.0 string value of this element, which is the
     * text itself.
     *
     * (Was the protected field `value`; the field is now the backing field of the
     * `value` property, writable by subclasses via the protected setter. `null` for a raw instance
     * made by the protected no-args constructor, as in jdom2.)
     */
    // XXX See http://www.servlets.com/archive/servlet/ReadMsg?msgId=8612
    // from elharo for a description of why Java characters may not suffice
    // long term
    override var value: String? = null
        protected set

    /**
     * CData type extends Text, and it needs to be able to change the Content
     * type of this Content.
     * @param ctype The CType to set for this Text-based Content.
     */
    protected constructor(ctype: CType) : super(ctype)

    /**
     * This is the protected, no-args constructor standard in all JDOM
     * classes. It allows subclassers to get a raw instance with no
     * initialization.
     */
    protected constructor() : this(CType.Text)

    /**
     * This constructor creates a new `Text` node, with the
     * supplied string value as its character content.
     *
     * @param str the node's character content.
     * @throws IllegalDataException if `str` contains an
     *         illegal character such as a vertical tab (as determined
     *         by [fi.sykero.kdom.Verifier.checkCharacterData])
     */
    constructor(str: String?) : this(CType.Text) {
        setText(str)
    }

    /**
     * This returns the value of this `Text` node as a Java
     * `String`.
     *
     * @return `String` - character content of this node.
     */
    val text: String?
        get() = value

    /**
     * This returns the textual content with all surrounding whitespace
     * removed.  If only whitespace exists, the empty string is returned.
     *
     * @return trimmed text content or empty string
     */
    val textTrim: String
        get() = Format.trimBoth(text)

    /**
     * This returns the textual content with all surrounding whitespace
     * removed and internal whitespace normalized to a single space.  If
     * only whitespace exists, the empty string is returned.
     *
     * @return normalized text content or empty string
     */
    val textNormalize: String
        get() = normalizeString(text)

    /**
     * This will set the value of this `Text` node.
     *
     * @param str value for node's content.
     * @return the object on which the method was invoked
     * @throws IllegalDataException if `str` contains an
     *         illegal character such as a vertical tab (as determined
     *         by [fi.sykero.kdom.Verifier.checkCharacterData])
     */
    open fun setText(str: String?): Text {
        val reason: String?

        if (str == null) {
            value = EMPTY_STRING
            return this
        }

        reason = Verifier.checkCharacterData(str)
        if (reason != null) {
            throw IllegalDataException(str, "character content", reason)
        }
        value = str
        return this
    }

    /**
     * This will append character content to whatever content already
     * exists within this `Text` node.
     *
     * @param str character content to append.
     * @throws IllegalDataException if `str` contains an
     *         illegal character such as a vertical tab (as determined
     *         by [fi.sykero.kdom.Verifier.checkCharacterData])
     */
    open fun append(str: String?) {
        val reason: String?

        if (str == null) {
            return
        }
        reason = Verifier.checkCharacterData(str)
        if (reason != null) {
            throw IllegalDataException(str, "character content", reason)
        }

        if (str.length > 0) {
            value += str
        }
    }

    /**
     * This will append the content of another `Text` node
     * to this node.
     *
     * @param text Text node to append.
     */
    open fun append(text: Text?) {
        if (text == null) {
            return
        }
        value += text.text
    }

    /**
     * This returns a `String` representation of the
     * `Text` node, suitable for debugging. If the XML
     * representation of the `Text` node is desired,
     * either `[getText]` or
     * [fi.sykero.kdom.output.XMLOutputter.outputString]`
     * should be used.
     *
     * @return `String` - information about this node.
     */
    override fun toString(): String {
        return StringBuilder(64)
            .append("[Text: ")
            .append(text)
            .append("]")
            .toString()
    }

    override fun clone(): Text {
        val text = super.clone() as Text
        text.value = value
        return text
    }

    override fun detach(): Text {
        return super.detach() as Text
    }

    override fun setParent(parent: Parent?): Text {
        return super.setParent(parent) as Text
    }

    override val parent: Element?
        get() =
            // because DocType can only be attached to a Document.
            super.parent as Element?

    companion object {
        /**
         * JDOM2 Serialization. In this case, Text is simple.
         */
        private const val serialVersionUID = 200L

        internal const val EMPTY_STRING = ""

        /**
         * This returns a new string with all surrounding whitespace
         * removed and internal whitespace normalized to a single space.  If
         * only whitespace exists, the empty string is returned.
         *
         * Per XML 1.0 Production 3 whitespace includes: #x20, #x9, #xD, #xA
         *
         * @param str string to be normalized.
         * @return normalized string or empty string
         */
        @JvmStatic
        fun normalizeString(str: String?): String {
            if (str == null)
                return EMPTY_STRING

            return Format.compact(str)
        }
    }

}

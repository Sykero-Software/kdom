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

/**
 * An XML CDATA section. Represents character-based content within an XML
 * document that should be output within special CDATA tags. Semantically it's
 * identical to a simple [Text] object, but output behavior is different.
 * CDATA makes no guarantees about the underlying textual representation of
 * character data, but does expose that data as a Java String.
 *
 * @author  Dan Schaffer
 * @author  Brett McLaughlin
 * @author  Jason Hunter
 * @author  Bradley S. Huffman
 * @author  Victor Toni
 * @author  Rolf Lear
 */
open class CDATA : Text {

    /**
     * This is the protected, no-args constructor standard in all JDOM
     * classes. It allows subclassers to get a raw instance with no
     * initialization.
     */
    protected constructor() : super(CType.CDATA)

    /**
     * This constructor creates a new `CDATA` node, with the
     * supplied string value as its character content.
     *
     * @param string the node's character content.
     * @throws IllegalDataException if `str` contains an
     *         illegal character such as a vertical tab (as determined
     *          by [fi.sykero.kdom.Verifier.checkCharacterData])
     *         or the CDATA end delimiter `]]&gt;`.
     */
    constructor(string: String?) : super(CType.CDATA) {
        setText(string)
    }

    /**
     * This will set the value of this `CDATA` node.
     *
     * @param str value for node's content.
     * @return the object on which the method was invoked
     * @throws IllegalDataException if `str` contains an
     *         illegal character such as a vertical tab (as determined
     *          by [fi.sykero.kdom.Verifier.checkCharacterData])
     *         or the CDATA end delimiter `]]&gt;`.
     */
    override fun setText(str: String?): CDATA {
        // Overrides Text.setText() because this needs to check that CDATA rules
        // are enforced. We could have a separate Verifier check for CDATA
        // beyond Text and call that alone before super.setText().

        if (str == null || "" == str) {
            textValue = EMPTY_STRING
            return this
        }

        val reason = Verifier.checkCDATASection(str)
        if (reason != null) {
            throw IllegalDataException(str, "CDATA section", reason)
        }

        textValue = str

        return this
    }

    /**
     * This will append character content to whatever content already
     * exists within this `CDATA` node.
     *
     * @param str character content to append.
     * @throws IllegalDataException if `str` contains an
     *         illegal character such as a vertical tab (as determined
     *          by [fi.sykero.kdom.Verifier.checkCharacterData])
     *         or the CDATA end delimiter `]]&gt;`.
     */
    override fun append(str: String?) {
        // Overrides Text.append(String) because this needs to check that CDATA
        // rules are enforced. We could have a separate Verifier check for CDATA
        // beyond Text and call that alone before super.setText().

        if (str == null || "" == str) {
            return
        }

        // we need a temp value to ensure that the value is changed _after_
        // validation
        val tmpValue: String
        if (textValue === EMPTY_STRING) {
            tmpValue = str
        } else {
            tmpValue = textValue + str
        }

        // we have to do late checking since the end of a CDATA section could
        // have been created by concatenating both strings:
        // "]" + "]>"
        // or
        // "]]" + ">"
        // TODO: maybe this could be optimized for this two cases
        val reason = Verifier.checkCDATASection(tmpValue)
        if (reason != null) {
            throw IllegalDataException(str, "CDATA section", reason)
        }

        textValue = tmpValue
    }

    /**
     * This will append the content of another `Text` node
     * to this node.
     *
     * @param text Text node to append.
     */
    override fun append(text: Text?) {
        // Overrides Text.append(Text) because this needs to check that CDATA
        // rules are enforced. We could have a separate Verifier check for CDATA
        // beyond Text and call that alone before super.setText().

        if (text == null) {
            return
        }
        append(text.text)
    }

    /**
     * This returns a `String` representation of the
     * `CDATA` node, suitable for debugging. If the XML
     * representation of the `CDATA` node is desired,
     * either `[getText]` or
     * [fi.sykero.kdom.output.XMLOutputter.output]`
     * should be used.
     *
     * @return `String` - information about this node.
     */
    override fun toString(): String {
        return StringBuilder(64)
            .append("[CDATA: ")
            .append(text)
            .append("]")
            .toString()
    }

    public override fun clone(): CDATA {
        return super.clone() as CDATA
    }

    override fun detach(): CDATA {
        return super.detach() as CDATA
    }

    override fun setParent(parent: Parent?): CDATA {
        return super.setParent(parent) as CDATA
    }

    private companion object {
        /**
         * JDOM 2.0.0 Serialization version. CDATA is simple
         */
        private const val serialVersionUID = 200L
    }

}

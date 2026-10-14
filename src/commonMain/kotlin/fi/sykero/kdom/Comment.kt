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

import fi.sykero.kdom.internal.unsafeNull

/**
 * An XML comment. Methods allow the user to get and set the text of the
 * comment.
 *
 * @author  Brett McLaughlin
 * @author  Jason Hunter
 */
open class Comment : Content {

    /**
     * This returns the textual data within the `Comment`.
     * (`Text` of the `Comment`; `null` for a raw instance made by the protected
     * no-args constructor. Was the protected field `text`.)
     *
     * @return `String` - text of comment.
     */
    var text: String
        get() = unsafeNull(textField)
        @JvmSynthetic
        set(text) {
            setText(text)
        }

    private var textField: String? = null

    /**
     * Default, no-args constructor for implementations to use if needed.
     */
    protected constructor() : super(CType.Comment)

    /**
     * This creates the comment with the supplied text.
     *
     * @param text `String` content of comment.
     */
    constructor(text: String?) : super(CType.Comment) {
        setText(text)
    }

    /**
     * Returns the XPath 1.0 string value of this element, which is the
     * text of this comment.
     *
     * @return the text of this comment
     */
    override val value: String
        get() = text

    /**
     * This will set the value of the `Comment`.
     *
     * @param text `String` text for comment.
     * @return `Comment` - this Comment modified.
     * @throws IllegalDataException if the given text is illegal for a
     *         Comment.
     */
    open fun setText(text: String?): Comment {
        val reason: String? = Verifier.checkCommentData(text)
        if (reason != null) {
            throw IllegalDataException(text, "comment", reason)
        }

        this.textField = text
        return this
    }

    protected override fun shallowCopy(): Comment {
        val copy = Comment()
        copy.textField = textField
        return copy
    }

    public override fun clone(): Comment {
        return super.clone() as Comment
    }

    override fun detach(): Comment {
        return super.detach() as Comment
    }

    internal override fun setParent(parent: Parent?): Comment {
        return super.setParent(parent) as Comment
    }

    /**
     * This returns a `String` representation of the
     * `Comment`, suitable for debugging. If the XML
     * representation of the `Comment` is desired,
     * [fi.sykero.kdom.output.XMLOutputter.outputString]
     * should be used.
     *
     * @return `String` - information about the
     *         `Comment`
     */
    override fun toString(): String {
        return StringBuilder()
            .append("[Comment: ")
            .append(fi.sykero.kdom.output.XMLOutputter().outputString(this))
            .append("]")
            .toString()
    }

}

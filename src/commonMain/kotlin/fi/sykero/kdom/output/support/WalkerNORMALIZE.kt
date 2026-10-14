/*-- 

 Copyright (C) 2012 Jason Hunter & Brett McLaughlin.
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
package fi.sykero.kdom.output.support

import fi.sykero.kdom.Content
import fi.sykero.kdom.Verifier

/**
 * This Walker implementation will produce trimmed text content.
 *
 * @author Rolf Lear
 */
class WalkerNORMALIZE
/**
 * Create the Trimmed walker instance.
 * @param content The list of content to format
 * @param fstack The current stack.
 * @param escape Whether Text values should be escaped.
 */
(content: List<Content>, fstack: FormatStack, escape: Boolean) :
    AbstractFormattedWalker(content, fstack, escape) {

    private fun isSpaceFirst(text: String): Boolean {
        if (text.length > 0) {
            return Verifier.isXMLWhitespace(text[0])
        }
        return false
    }

    private fun isSpaceLast(text: String): Boolean {
        val tlen = text.length
        if (tlen > 0 && Verifier.isXMLWhitespace(text[tlen - 1])) {
            return true
        }
        return false
    }

    override fun analyzeMultiText(mtext: MultiText, offset: Int, len: Int) {
        var needspace = false
        var between = false

        var ttext: String?
        for (i in 0 until len) {
            val c = get(offset + i)
            when (c.cType) {
                Content.CType.Text -> {
                    ttext = c.value
                    if (Verifier.isAllXMLWhitespace(ttext)) {
                        if (between && ttext.length > 0) {
                            needspace = true
                        }
                    } else {
                        if (between && (needspace || isSpaceFirst(ttext))) {
                            mtext.appendText(Trim.NONE, " ")
                        }
                        mtext.appendText(Trim.COMPACT, ttext)
                        between = true
                        needspace = isSpaceLast(ttext)
                    }
                }
                Content.CType.CDATA -> {
                    ttext = c.value
                    if (Verifier.isAllXMLWhitespace(ttext)) {
                        if (between && ttext.length > 0) {
                            needspace = true
                        }
                    } else {
                        if (between && (needspace || isSpaceFirst(ttext))) {
                            mtext.appendText(Trim.NONE, " ")
                        }
                        mtext.appendCDATA(Trim.COMPACT, ttext)
                        between = true
                        needspace = isSpaceLast(ttext)
                    }
                }
                // EntityRef: treat like any other content, raw.
                else -> {
                    ttext = null
                    if (between && needspace) {
                        mtext.appendText(Trim.NONE, " ")
                    }
                    mtext.appendRaw(c)
                    between = true
                    needspace = false
                }
            }
        }
    }
}

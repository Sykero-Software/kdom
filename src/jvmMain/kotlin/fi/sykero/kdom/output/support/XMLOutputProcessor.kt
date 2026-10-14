/*-- 

 Copyright (C) 2000-2007 Jason Hunter & Brett McLaughlin.
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

 Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software.

 */
package fi.sykero.kdom.output.support

import fi.sykero.kdom.Attribute
import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Content
import fi.sykero.kdom.DocType
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.EntityRef
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text
import fi.sykero.kdom.output.Format
import fi.sykero.kdom.output.XMLOutputter
import fi.sykero.kdom.internal.IOException

/**
 * This interface provides a base support for the [XMLOutputter].
 *
 * People who want to create a custom XMLOutputProcessor for XMLOutputter are
 * able to implement this interface with the following notes and restrictions:
 * <ol>
 * <li>The XMLOutputter will call one, and only one of the `process(Writer,Format,*)` methods each
 * time the XMLOutputter is requested to output some JDOM content. It is thus
 * safe to assume that a `process(Writer,Format,*)` method can set up any
 * infrastructure needed to process the content, and that the XMLOutputter will
 * not re-call that method, or some other `process(Writer,Format,*)` method for the same output
 * sequence.
 * <li>The process methods should be thread-safe and reentrant: The same
 * `process(Writer,Format,*)` method may (will) be called concurrently from different threads.
 * </ol>
 *
 * The [AbstractXMLOutputProcessor] class is a full implementation of this
 * interface and is fully customisable. People who want a custom XMLOutputter
 * are encouraged to extend the AbstractXMLOutputProcessor rather than do a full
 * re-implementation of this interface.
 * 
 * @see XMLOutputter
 * @see AbstractXMLOutputProcessor
 * @since JDOM2
 * @author Rolf Lear
 */
interface XMLOutputProcessor {

    /**
     * This will print the `[Document]` to the given Writer.
     *
     * Warning: using your own Writer may cause the outputter's preferred
     * character encoding to be ignored. If you use encodings other than UTF-8,
     * we recommend using the method that takes an OutputStream instead.
     *
     * 
     * @param out
     *        `Writer` to use.
     * @param format
     *        `Format` instance specifying output style
     * @param doc
     *        `Document` to format.
     * @throws IOException
     *         if there's any problem writing.
     * @throws NullPointerException
     *         if the input content is null
     */
    @Throws(IOException::class)
    fun process(out: Appendable, format: Format, doc: Document)

    /**
     * Print out the `[DocType]`.
     * 
     * @param out
     *        `Writer` to use.
     * @param format
     *        `Format` instance specifying output style
     * @param doctype
     *        `DocType` to output.
     * @throws IOException
     *         if there's any problem writing.
     * @throws NullPointerException
     *         if the input content is null
     */
    @Throws(IOException::class)
    fun process(out: Appendable, format: Format, doctype: DocType)

    /**
     * Print out an `[Element]`, including its
     * `[Attribute]`s, and all contained (child) elements, etc.
     * 
     * @param out
     *        `Writer` to use.
     * @param format
     *        `Format` instance specifying output style
     * @param element
     *        `Element` to output.
     * @throws IOException
     *         if there's any problem writing.
     * @throws NullPointerException
     *         if the input content is null
     */
    @Throws(IOException::class)
    fun process(out: Appendable, format: Format, element: Element)

    /**
     * This will handle printing out a list of nodes. This can be useful for
     * printing the content of an element that contains HTML, like
     * "&lt;description&gt;JDOM is &lt;b&gt;fun&gt;!&lt;/description&gt;".
     * 
     * @param out
     *        `Writer` to use.
     * @param format
     *        `Format` instance specifying output style
     * @param list
     *        `List` of nodes.
     * @throws IOException
     *         if there's any problem writing.
     * @throws NullPointerException
     *         if the input list is null or contains null members
     * @throws ClassCastException
     *         if any of the list members are not [Content]
     */
    @Throws(IOException::class)
    fun process(out: Appendable, format: Format, list: List<Content>)

    /**
     * Print out a `[CDATA]` node.
     * 
     * @param out
     *        `Writer` to use.
     * @param format
     *        `Format` instance specifying output style
     * @param cdata
     *        `CDATA` to output.
     * @throws IOException
     *         if there's any problem writing.
     * @throws NullPointerException
     *         if the input content is null
     */
    @Throws(IOException::class)
    fun process(out: Appendable, format: Format, cdata: CDATA)

    /**
     * Print out a `[Text]` node. Performs the necessary entity
     * escaping and whitespace stripping.
     * 
     * @param out
     *        `Writer` to use.
     * @param format
     *        `Format` instance specifying output style
     * @param text
     *        `Text` to output.
     * @throws IOException
     *         if there's any problem writing.
     * @throws NullPointerException
     *         if the input content is null
     */
    @Throws(IOException::class)
    fun process(out: Appendable, format: Format, text: Text)

    /**
     * Print out a `[Comment]`.
     * 
     * @param out
     *        `Writer` to use.
     * @param format
     *        `Format` instance specifying output style
     * @param comment
     *        `Comment` to output.
     * @throws IOException
     *         if there's any problem writing.
     * @throws NullPointerException
     *         if the input content is null
     */
    @Throws(IOException::class)
    fun process(out: Appendable, format: Format, comment: Comment)

    /**
     * Print out a `[ProcessingInstruction]`.
     * 
     * @param out
     *        `Writer` to use.
     * @param format
     *        `Format` instance specifying output style
     * @param pi
     *        `ProcessingInstruction` to output.
     * @throws IOException
     *         if there's any problem writing.
     * @throws NullPointerException
     *         if the input content is null
     */
    @Throws(IOException::class)
    fun process(out: Appendable, format: Format, pi: ProcessingInstruction)

    /**
     * Print out a `[EntityRef]`.
     * 
     * @param out
     *        `Writer` to use.
     * @param format
     *        `Format` instance specifying output style
     * @param entity
     *        `EntityRef` to output.
     * @throws IOException
     *         if there's any problem writing.
     * @throws NullPointerException
     *         if the input content is null
     */
    @Throws(IOException::class)
    fun process(out: Appendable, format: Format, entity: EntityRef)

}

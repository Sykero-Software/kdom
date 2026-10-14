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
package fi.sykero.kdom.output

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
import fi.sykero.kdom.output.support.AbstractXMLOutputProcessor
import fi.sykero.kdom.output.support.FormatStack
import fi.sykero.kdom.output.support.XMLOutputProcessor
import fi.sykero.kdom.internal.IOException
import fi.sykero.kdom.internal.flushIfPossible

/**
 * Outputs a JDOM document as a stream of bytes.
 *
 * The XMLOutputter can manage many styles of document formatting, from
 * untouched to 'pretty' printed. The default is to output the document content
 * exactly as created, but this can be changed by setting a new Format object:
 *
 *  * For pretty-print output, use
 * [Format.getPrettyFormat].
 *  * For whitespace-normalised output, use
 * [Format.getCompactFormat].
 *  * For unmodified-format output, use
 * [Format.getRawFormat].
 *
 *
 * There are [output] methods to print any of
 * the standard JDOM classes to either a Writer or an OutputStream.
 *
 * **Warning**: When outputting to a Writer, make sure the writer's encoding
 * matches the encoding setting in the Format object. This ensures the encoding
 * in which the content is written (controlled by the Writer configuration)
 * matches the XML Character escaping as well as the encoding placed in the
 * document's XML declaration (controlled by the XMLOutputter).
 * Because a Writer cannot be queried for its encoding, the
 * information must be passed to the Format manually in its constructor or via
 * the [Format.setEncoding] method. The default encoding is
 * UTF-8. If the default encoding of the platform is not UTF-8 and a default Format
 * encoding is used, then there may be incorrectly formed characters in the output.
 *
 * The methods [outputString] are for
 * convenience only; for top performance you should call one of the `{@link
 * #output output(...)}` methods and pass in your own Writer or
 * OutputStream if possible.
 *
 * **All** of the `output*(...)` methods will flush the
 * destination Writer or OutputStream before returning, and **none** of them
 * will `close()` the destination.
 *
 * XML declarations are always printed on their own line followed by a line
 * separator (this doesn't change the semantics of the document). To omit
 * printing of the declaration use
 * [Format.setOmitDeclaration]. To omit printing of the
 * encoding in the declaration use [Format.setOmitEncoding].
 * Unfortunately there is currently no way to know the original encoding of the
 * document.
 *
 * Empty elements are by default printed as &lt;empty/&gt;, but this can be
 * configured with [Format.setExpandEmptyElements] to cause
 * them to be expanded to &lt;empty&gt;&lt;/empty&gt;.
 *
 * If changing the [Format] settings are insufficient for your output
 * needs you can customise this XMLOutputter further by setting a different
 * [XMLOutputProcessor] with the
 * [setXMLOutputProcessor] method or an appropriate
 * constructor. A fully-enabled Abstract class
 * [AbstractXMLOutputProcessor] is available to be further extended to
 * your needs if all you want to do is tweak some details.
 * 
 * @author Brett McLaughlin
 * @author Jason Hunter
 * @author Jason Reid
 * @author Wolfgang Werner
 * @author Elliotte Rusty Harold
 * @author David &amp; Will (from Post Tool Design)
 * @author Dan Schaffer
 * @author Alex Chaffee
 * @author Bradley S. Huffman
 * @author Rolf Lear
 */
class XMLOutputter(format: Format?, processor: XMLOutputProcessor?) {

    /**
     * Create a final and static instance of the AbstractXMLOutputProcessor The
     * final part is important because it improves performance.
     *
     * The JDOM user can change the actual XMLOutputProcessor with the
     * [XMLOutputter.setXMLOutputProcessor] method.
     *
     * @author rolf
     */
    private class DefaultXMLProcessor : AbstractXMLOutputProcessor() {

        /**
         * A helper method to implement backward-compatibility with JDOM1
         *
         * @see XMLOutputter.escapeAttributeEntities
         * @param str
         * The String to output.
         * @param format
         * The format details to use.
         * @return The input String escaped as an attribute value.
         */
        fun escapeAttributeEntities(str: String, format: Format): String {
            val sw = StringBuilder()
            try {
                super.attributeEscapedEntitiesFilter(sw, FormatStack(format), str)
            } catch (e: IOException) {
                // no IOException on StringWriter....
            }
            return sw.toString()
        }

        /**
         * A helper method to implement backward-compatibility with JDOM1
         *
         * @see XMLOutputter.escapeElementEntities
         * @param str
         * The String to output.
         * @param format
         * The format details to use.
         * @return The input String escaped as an element text value.
         */
        fun escapeElementEntities(str: String, format: Format): String {
            return Format.escapeText(
                format.escapeStrategy,
                format.lineSeparator, str
            )
        }
    }

    /*
     * =====================================================================
     * Instance content.
     * =====================================================================
     */

    // For normal output
    private var myFormat: Format = if (format == null) Format.getRawFormat() else format.clone()

    // The actual XMLOutputProcessor to delegate to.
    private var myProcessor: XMLOutputProcessor = processor ?: DEFAULTPROCESSOR

    /*
     * =====================================================================
     * Constructors
     * =====================================================================
     */

    /**
     * This will create an `XMLOutputter` with a default
     * [Format] and [XMLOutputProcessor].
     */
    constructor() : this(null, null)

    /**
     * This will create an `XMLOutputter` with the same
     * customisations set in the given `XMLOutputter` instance. Note
     * that `XMLOutputter two = one.clone();` would work equally
     * well.
     *
     * @param that
     * the XMLOutputter to clone
     */
    constructor(that: XMLOutputter) : this(that.myFormat, null)

    /**
     * This will create an `XMLOutputter` with the specified format
     * characteristics.
     *
     * **Note:** the format object is cloned internally before use.
     *
     * @param format
     * The Format instance to use. This instance will be cloned() and as
     * a consequence, changes made to the specified format instance
     * **will not** be reflected in this XMLOutputter. A null input
     * format indicates that XMLOutputter should use the default
     * [Format.getRawFormat]
     */
    constructor(format: Format?) : this(format, null)

    /**
     * This will create an `XMLOutputter` with the specified
     * XMLOutputProcessor.
     *
     * @param processor
     * The XMLOutputProcessor to delegate output to. If null the
     * XMLOutputter will use the default XMLOutputProcessor.
     */
    constructor(processor: XMLOutputProcessor?) : this(null, processor)

    /*
     * =======================================================================
     * API - Settings...
     * =======================================================================
     */

    /**
     * The current format in use by the XMLOutputter. Setting it sets the new
     * format logic for the XMLOutputter: the Format object is
     * cloned internally before use. Note the Format
     * object returned by the getter is **not** a clone of the one used
     * internally, thus, an XMLOutputter instance is able to have its Format
     * changed by changing the settings on the Format instance returned.
     */
    var format: Format
        get() = myFormat
        set(newFormat) {
            this.myFormat = newFormat.clone()
        }

    /**
     * The current XMLOutputProcessor instance in use by the
     * XMLOutputter. Setting it sets a new XMLOutputProcessor instance for this
     * XMLOutputter. Note the processor object is expected to be thread-safe.
     *
     * (The JVM names stay `getXMLOutputProcessor`/`setXMLOutputProcessor`.)
     */
    var xmlOutputProcessor: XMLOutputProcessor
        @JvmName("getXMLOutputProcessor")
        get() = myProcessor
        @JvmName("setXMLOutputProcessor")
        set(processor) {
            this.myProcessor = processor
        }

    /*
     * =======================================================================
     * API - Output to STRING Methods ... All methods defer to the WRITER
     * equivalents
     * =======================================================================
     */

    /**
     * Return a string representing the Document. Warning: a String is Unicode, which
     * may not match the outputter's specified encoding.
     *
     * @param doc the content to output.
     * @return the content as a String.
     */
    fun outputString(doc: Document): String {
        val out = StringBuilder()
        try {
            output(doc, out) // output() flushes
        } catch (e: IOException) {
            // swallow - will never happen.
        }
        return out.toString()
    }

    /**
     * Return a string representing the DocType. Warning: a String is Unicode, which
     * may not match the outputter's specified encoding.
     *
     * @param doctype the content to output.
     * @return the content as a String.
     */
    fun outputString(doctype: DocType): String {
        val out = StringBuilder()
        try {
            output(doctype, out) // output() flushes
        } catch (e: IOException) {
            // swallow - will never happen.
        }
        return out.toString()
    }

    /**
     * Return a string representing the Element. Warning: a String is Unicode, which
     * may not match the outputter's specified encoding.
     *
     * @param element the content to output.
     * @return the content as a String.
     */
    fun outputString(element: Element): String {
        val out = StringBuilder()
        try {
            output(element, out) // output() flushes
        } catch (e: IOException) {
            // swallow - will never happen.
        }
        return out.toString()
    }

    /**
     * Return a string representing the list of Content. Warning: a String is Unicode, which
     * may not match the outputter's specified encoding.
     *
     * @param list the content to output.
     * @return the content as a String.
     */
    fun outputString(list: List<Content>): String {
        val out = StringBuilder()
        try {
            output(list, out) // output() flushes
        } catch (e: IOException) {
            // swallow - will never happen.
        }
        return out.toString()
    }

    /**
     * Return a string representing the CDATA. Warning: a String is Unicode, which
     * may not match the outputter's specified encoding.
     *
     * @param cdata the content to output.
     * @return the content as a String.
     */
    fun outputString(cdata: CDATA): String {
        val out = StringBuilder()
        try {
            output(cdata, out) // output() flushes
        } catch (e: IOException) {
            // swallow - will never happen.
        }
        return out.toString()
    }

    /**
     * Return a string representing the Text. Warning: a String is Unicode, which
     * may not match the outputter's specified encoding.
     *
     * @param text the content to output.
     * @return the content as a String.
     */
    fun outputString(text: Text): String {
        val out = StringBuilder()
        try {
            output(text, out) // output() flushes
        } catch (e: IOException) {
            // swallow - will never happen.
        }
        return out.toString()
    }

    /**
     * Return a string representing the Comment. Warning: a String is Unicode, which
     * may not match the outputter's specified encoding.
     *
     * @param comment the content to output.
     * @return the content as a String.
     */
    fun outputString(comment: Comment): String {
        val out = StringBuilder()
        try {
            output(comment, out) // output() flushes
        } catch (e: IOException) {
            // swallow - will never happen.
        }
        return out.toString()
    }

    /**
     * Return a string representing the ProcessingInstruction. Warning: a String is Unicode, which
     * may not match the outputter's specified encoding.
     *
     * @param pi the content to output.
     * @return the content as a String.
     */
    fun outputString(pi: ProcessingInstruction): String {
        val out = StringBuilder()
        try {
            output(pi, out) // output() flushes
        } catch (e: IOException) {
            // swallow - will never happen.
        }
        return out.toString()
    }

    /**
     * Return a string representing the EntityRef. Warning: a String is Unicode, which
     * may not match the outputter's specified encoding.
     *
     * @param entity the content to output.
     * @return the content as a String.
     */
    fun outputString(entity: EntityRef): String {
        val out = StringBuilder()
        try {
            output(entity, out) // output() flushes
        } catch (e: IOException) {
            // swallow - will never happen.
        }
        return out.toString()
    }

    /**
     * Return a string representing an element's content only (no tag, no attributes).
     *
     * @param element `Element` to output.
     * @return the element content as a String.
     */
    fun outputElementContentString(element: Element): String {
        val out = StringBuilder()
        try {
            outputElementContent(element, out) // output() flushes
        } catch (e: IOException) {
            // swallow - will never happen.
        }
        return out.toString()
    }

    /*
     * =======================================================================
     * API - Output to WRITER Methods ... All methods defer to the processor.
     * =======================================================================
     */

    /**
     * This will print the Document to the given Writer. Warning: using your own
     * Writer may cause the outputter's preferred character encoding to be ignored.
     *
     * @param doc the content to output.
     * @param out `Appendable` (for example a `Writer` or a `StringBuilder`) to use.
     * @throws IOException if there's any problem writing.
     * @throws NullPointerException if the specified content is null.
     */
    @Throws(IOException::class)
    fun output(doc: Document, out: Appendable) {
        myProcessor.process(out, myFormat, doc)
        flushIfPossible(out)
    }

    /**
     * This will print the DocType to the given Writer. Warning: using your own
     * Writer may cause the outputter's preferred character encoding to be ignored.
     *
     * @param doctype the content to output.
     * @param out `Appendable` (for example a `Writer` or a `StringBuilder`) to use.
     * @throws IOException if there's any problem writing.
     * @throws NullPointerException if the specified content is null.
     */
    @Throws(IOException::class)
    fun output(doctype: DocType, out: Appendable) {
        myProcessor.process(out, myFormat, doctype)
        flushIfPossible(out)
    }

    /**
     * This will print the Element to the given Writer. Warning: using your own
     * Writer may cause the outputter's preferred character encoding to be ignored.
     *
     * @param element the content to output.
     * @param out `Appendable` (for example a `Writer` or a `StringBuilder`) to use.
     * @throws IOException if there's any problem writing.
     * @throws NullPointerException if the specified content is null.
     */
    @Throws(IOException::class)
    fun output(element: Element, out: Appendable) {
        // If this is the root element we could pre-initialize the
        // namespace stack with the namespaces
        myProcessor.process(out, myFormat, element)
        flushIfPossible(out)
    }

    /**
     * This will handle printing out an [Element]'s content only, not including its tag, and attributes.
     *
     * @param element `Element` to output.
     * @param out `Appendable` (for example a `Writer` or a `StringBuilder`) to use.
     * @throws IOException if there's any problem writing.
     * @throws NullPointerException if the specified content is null.
     */
    @Throws(IOException::class)
    fun outputElementContent(element: Element, out: Appendable) {
        myProcessor.process(out, myFormat, element.content)
        flushIfPossible(out)
    }

    /**
     * This will print the list of Content to the given Writer. Warning: using your own
     * Writer may cause the outputter's preferred character encoding to be ignored.
     *
     * @param list the content to output.
     * @param out `Appendable` (for example a `Writer` or a `StringBuilder`) to use.
     * @throws IOException if there's any problem writing.
     * @throws NullPointerException if the specified content is null.
     */
    @Throws(IOException::class)
    fun output(list: List<Content>, out: Appendable) {
        myProcessor.process(out, myFormat, list)
        flushIfPossible(out)
    }

    /**
     * This will print the CDATA to the given Writer. Warning: using your own
     * Writer may cause the outputter's preferred character encoding to be ignored.
     *
     * @param cdata the content to output.
     * @param out `Appendable` (for example a `Writer` or a `StringBuilder`) to use.
     * @throws IOException if there's any problem writing.
     * @throws NullPointerException if the specified content is null.
     */
    @Throws(IOException::class)
    fun output(cdata: CDATA, out: Appendable) {
        myProcessor.process(out, myFormat, cdata)
        flushIfPossible(out)
    }

    /**
     * This will print the Text to the given Writer. Warning: using your own
     * Writer may cause the outputter's preferred character encoding to be ignored.
     *
     * @param text the content to output.
     * @param out `Appendable` (for example a `Writer` or a `StringBuilder`) to use.
     * @throws IOException if there's any problem writing.
     * @throws NullPointerException if the specified content is null.
     */
    @Throws(IOException::class)
    fun output(text: Text, out: Appendable) {
        myProcessor.process(out, myFormat, text)
        flushIfPossible(out)
    }

    /**
     * This will print the Comment to the given Writer. Warning: using your own
     * Writer may cause the outputter's preferred character encoding to be ignored.
     *
     * @param comment the content to output.
     * @param out `Appendable` (for example a `Writer` or a `StringBuilder`) to use.
     * @throws IOException if there's any problem writing.
     * @throws NullPointerException if the specified content is null.
     */
    @Throws(IOException::class)
    fun output(comment: Comment, out: Appendable) {
        myProcessor.process(out, myFormat, comment)
        flushIfPossible(out)
    }

    /**
     * This will print the ProcessingInstruction to the given Writer. Warning: using your own
     * Writer may cause the outputter's preferred character encoding to be ignored.
     *
     * @param pi the content to output.
     * @param out `Appendable` (for example a `Writer` or a `StringBuilder`) to use.
     * @throws IOException if there's any problem writing.
     * @throws NullPointerException if the specified content is null.
     */
    @Throws(IOException::class)
    fun output(pi: ProcessingInstruction, out: Appendable) {
        myProcessor.process(out, myFormat, pi)
        flushIfPossible(out)
    }

    /**
     * This will print the EntityRef to the given Writer. Warning: using your own
     * Writer may cause the outputter's preferred character encoding to be ignored.
     *
     * @param entity the content to output.
     * @param out `Appendable` (for example a `Writer` or a `StringBuilder`) to use.
     * @throws IOException if there's any problem writing.
     * @throws NullPointerException if the specified content is null.
     */
    @Throws(IOException::class)
    fun output(entity: EntityRef, out: Appendable) {
        myProcessor.process(out, myFormat, entity)
        flushIfPossible(out)
    }
    /*
     * =======================================================================
     * API - Escaping
     * =======================================================================
     */

    /**
     * Escape any characters in the String that must be escaped in an attribute value.
     * (JDOM1 backward-compatibility.)
     *
     * @param str The String to escape.
     * @return the escaped String.
     */
    fun escapeAttributeEntities(str: String): String {
        return DEFAULTPROCESSOR.escapeAttributeEntities(str, myFormat)
    }

    /**
     * Escape any characters in the String that must be escaped in element text.
     * (JDOM1 backward-compatibility.)
     *
     * @param str The String to escape.
     * @return the escaped String.
     */
    fun escapeElementEntities(str: String): String {
        return DEFAULTPROCESSOR.escapeElementEntities(str, myFormat)
    }

    /*
     * =======================================================================
     * Basic support methods.
     * =======================================================================
     */

    /**
     * Returns a cloned copy of this XMLOutputter.
     */
    fun clone(): XMLOutputter {
        // Implementation notes: Since all state of an XMLOutputter is
        // embodied in simple private instance variables, Object.clone
        // can be used.
        // kdom: no Object.clone() in common code; the copy shares the Format and the processor instances, as
        // jdom2's shallow Object.clone() did.
        val copy = XMLOutputter(null as Format?, myProcessor)
        copy.myFormat = myFormat
        return copy
    }

    /**
     * Return a string listing of the settings for this
     * XMLOutputter instance.
     *
     * @return a string listing the settings for this XMLOutputter instance
     */
    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append("XMLOutputter[omitDeclaration = ")
        buffer.append(myFormat.omitDeclaration)
        buffer.append(", ")
        buffer.append("encoding = ")
        buffer.append(myFormat.encoding)
        buffer.append(", ")
        buffer.append("omitEncoding = ")
        buffer.append(myFormat.omitEncoding)
        buffer.append(", ")
        buffer.append("indent = '")
        buffer.append(myFormat.indent)
        buffer.append("'")
        buffer.append(", ")
        buffer.append("expandEmptyElements = ")
        buffer.append(myFormat.expandEmptyElements)
        buffer.append(", ")
        buffer.append("lineSeparator = '")
        for (ch in myFormat.lineSeparator!!.toCharArray()) {
            when (ch) {
                '\r' -> buffer.append("\\r")
                '\n' -> buffer.append("\\n")
                '\t' -> buffer.append("\\t")
                else -> buffer.append("[" + ch.code + "]")
            }
        }
        buffer.append("', ")
        buffer.append("textMode = ")
        buffer.append(myFormat.textMode.toString() + "]")
        return buffer.toString()
    }

    private companion object {

        /*
         * =====================================================================
         * Static content.
         * =====================================================================
         */

        /**
         * This constant XMLOutputProcessor is used for all non-customised
         * XMLOutputters
         */
        private val DEFAULTPROCESSOR = DefaultXMLProcessor()
    }
}

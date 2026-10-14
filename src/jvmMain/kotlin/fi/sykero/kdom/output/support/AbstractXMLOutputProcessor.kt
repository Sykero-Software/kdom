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
import fi.sykero.kdom.IllegalDataException
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text
import fi.sykero.kdom.Verifier
import fi.sykero.kdom.output.Format
import fi.sykero.kdom.output.Format.TextMode
import fi.sykero.kdom.output.XMLOutputter
import fi.sykero.kdom.util.NamespaceStack
import fi.sykero.kdom.internal.IOException
import fi.sykero.kdom.internal.flushIfPossible

/**
 * This class provides a concrete implementation of [XMLOutputProcessor]
 * for supporting the [XMLOutputter].
 *
 * ## Overview
 *
 * This class is marked abstract even though all methods are fully implemented.
 * The `process*(...)` methods are public because they match the
 * XMLOutputProcessor interface but the remaining methods are all protected.
 *
 * People who want to create a custom XMLOutputProcessor for XMLOutputter are
 * able to extend this class and modify any functionality they want. Before
 * sub-classing this you should first check to see if the [Format] class
 * can get you the results you want.
 *
 * **Subclasses of this should have reentrant methods.** This is
 * easiest to accomplish simply by not allowing any instance fields. If your
 * sub-class has an instance field/variable, then it's probably broken.
 *
 * ## The Stacks
 *
 * One significant feature of this implementation is that it creates and
 * maintains both a [NamespaceStack] and [FormatStack] that are
 * managed in the
 * [printElement] method.
 * The stacks are pushed and popped in that method only. They significantly
 * improve the performance and readability of the code.
 *
 * The NamespaceStack is only sent through to the
 * [printElement] and
 * [printContent] methods,
 * but the FormatStack is pushed through to all print* Methods.
 *
 * ## Text Processing
 *
 * In XML the concept of 'Text' can be loosely defined as anything that can be
 * found between an Element's start and end tags, excluding Comments and
 * Processing Instructions. When considered from a JDOM perspective, this means
 * [Text], [CDATA] and [EntityRef] content. This will be
 * referred to as 'Text-like content'
 *
 * XMLOutputter delegates the management and formatting of Content to a
 * Walker instance. See [Walker] and its various implementations for
 * details on how the Element content is processed.
 *
 * Because the Walker interface specifies that Text/CDATA content may be
 * returned as either Text/CDATA instances or as formatted String values
 * this class sometimes uses printCDATA(...) and printText(...), and sometimes
 * uses the more direct [textCDATA] or
 * [textRaw] as
 * appropriate. In other words, subclasses should probably override these second
 * methods instead of the print methods.
 *
 * ## Non-Text Content
 *
 * Non-text content is processed via the respective print* methods. The usage
 * should be logical based on the method name.
 *
 * The general observations are:
 *
 *  * printElement - maintains the Stacks, prints the element open tags, with
 * attributes and namespaces. It checks to see whether the Element is text-only,
 * or has non-text content. If it is text-only there is no indent/newline
 * handling and it delegates to the correct text-type print method, otherwise it
 * delegates to printContent.
 *  * printContent is called to output all lists of Content. It assumes that
 * all whitespace indentation/newlines are appropriate before it is called, but
 * it will ensure that padding is appropriate between the items in the list.
 *
 *
 * ## Final Notes No methods actually write to the destination Writer
 * except the `write(...)` methods. Thus, all other methods do their
 * respective processing and delegate the actual destination output to the
 * [write] or [write] methods.
 *
 * All Text-like content (printCDATA, printText, and printEntityRef) will
 * ultimately be output through the the text* methods (and no other content).
 *
 *
 * @see XMLOutputter
 * @see XMLOutputProcessor
 * @since JDOM2
 * @author Rolf Lear
 */
abstract class AbstractXMLOutputProcessor : AbstractOutputProcessor(), XMLOutputProcessor {

    /* *******************************************
     * XMLOutputProcessor implementation.
     * *******************************************
     */

    @Throws(IOException::class)
    override fun process(out: Appendable, format: Format, doc: Document) {
        printDocument(out, FormatStack(format), NamespaceStack(), doc)
        flushIfPossible(out)
    }

    @Throws(IOException::class)
    override fun process(out: Appendable, format: Format, doctype: DocType) {
        printDocType(out, FormatStack(format), doctype)
        flushIfPossible(out)
    }

    @Throws(IOException::class)
    override fun process(out: Appendable, format: Format, element: Element) {
        // If this is the root element we could pre-initialize the
        // namespace stack with the namespaces
        printElement(
            out, FormatStack(format), NamespaceStack(),
            element
        )
        flushIfPossible(out)
    }

    @Throws(IOException::class)
    override fun process(out: Appendable, format: Format, list: List<Content>) {
        val fstack = FormatStack(format)
        val walker = buildWalker(fstack, list, true)
        printContent(out, fstack, NamespaceStack(), walker)
        flushIfPossible(out)
    }

    @Throws(IOException::class)
    override fun process(out: Appendable, format: Format, cdata: CDATA) {
        // we use the powers of the Walker to manage text-like content.
        val list: List<CDATA> = listOf(cdata)
        val fstack = FormatStack(format)
        val walker = buildWalker(fstack, list, true)
        if (walker.hasNext()) {
            printContent(out, fstack, NamespaceStack(), walker)
        }
        flushIfPossible(out)
    }

    @Throws(IOException::class)
    override fun process(out: Appendable, format: Format, text: Text) {
        // we use the powers of the Walker to manage text-like content.
        val list: List<Text> = listOf(text)
        val fstack = FormatStack(format)
        val walker = buildWalker(fstack, list, true)
        if (walker.hasNext()) {
            printContent(out, fstack, NamespaceStack(), walker)
        }
        flushIfPossible(out)
    }

    @Throws(IOException::class)
    override fun process(out: Appendable, format: Format, comment: Comment) {
        printComment(out, FormatStack(format), comment)
        flushIfPossible(out)
    }

    @Throws(IOException::class)
    override fun process(out: Appendable, format: Format, pi: ProcessingInstruction) {
        val fstack = FormatStack(format)
        // Output PI verbatim, disregarding TrAX escaping PIs.
        fstack.isIgnoreTrAXEscapingPIs = true
        printProcessingInstruction(out, fstack, pi)
        flushIfPossible(out)
    }

    @Throws(IOException::class)
    override fun process(out: Appendable, format: Format, entity: EntityRef) {
        printEntityRef(out, FormatStack(format), entity)
        flushIfPossible(out)
    }

    /*
     * ========================================================================
     * Methods that actually write data to output. None of the other methods
     * should directly write to the output unless they use these methods.
     * ========================================================================
     */

    /**
     * Print some string value to the output. Null values are ignored. This
     * ignore-null property is used for a few tricks.
     *
     * @param out
     * The Writer to write to.
     * @param str
     * The String to write (can be null).
     * @throws IOException
     * if the out Writer fails.
     */
    @Throws(IOException::class)
    protected open fun write(out: Appendable, str: String?) {
        if (str == null) {
            return
        }
        out.append(str)
    }

    /**
     * Write a single character to the output Writer.
     *
     * @param out
     * The Writer to write to.
     * @param c
     * The char to write.
     * @throws IOException
     * if the Writer fails.
     */
    @Throws(IOException::class)
    protected open fun write(out: Appendable, c: Char) {
        out.append(c)
    }

    /*
     * ========================================================================
     * Support methods for Text-content formatting. Should all be protected. The
     * following are used when printing Text-based data. Because of complicated
     * multi-sequential text sometimes the requirements are odd. All Text
     * content will be output using these methods, which is why there is the None
     * version.
     * ========================================================================
     */

    /**
     * This will take the three pre-defined entities in XML 1.0 ('&lt;', '&gt;',
     * and '&amp;' - used specifically in XML elements) as well as CR/NL and
     * Quote characters which require escaping inside Attribute values and
     * convert their character representation to the appropriate entity
     * reference suitable for XML attribute content. Further, some special
     * characters (e.g. characters that are not valid in the current encoding)
     * are converted to escaped representations.
     *
     * **Note:** If [FormatStack.escapeOutput] is false then no
     * escaping will happen.
     *
     * @param out
     * The destination Writer
     * @param fstack
     * The [FormatStack]
     * @param value
     * `String` Attribute value to escape.
     * @throws IOException
     * if the destination Writer fails.
     * @throws IllegalDataException
     * if an entity can not be escaped
     */
    @Throws(IOException::class)
    protected open fun attributeEscapedEntitiesFilter(
        out: Appendable,
        fstack: FormatStack, value: String?
    ) {

        if (!fstack.escapeOutput) {
            // no escaping...
            write(out, value)
            return
        }

        write(out, Format.escapeAttribute(fstack.escapeStrategy, value!!))
    }

    /**
     * Convenience method that simply passes the input str to
     * [write]. This could be useful for subclasses to
     * hook in to. All text-type output will come through this or the
     * [textRaw] method taking a char.
     *
     * @param out
     * the destination writer.
     * @param str
     * the String to write.
     * @throws IOException
     * if the Writer fails.
     */
    @Throws(IOException::class)
    protected open fun textRaw(out: Appendable, str: String?) {
        write(out, str)
    }

    /**
     * Convenience method that simply passes the input char to
     * [write]. This could be useful for subclasses to hook
     * in to. All text-type output will come through this or the
     * [textRaw] method taking a String.
     *
     * @param out
     * the destination Writer.
     * @param ch
     * the char to write.
     * @throws IOException
     * if the Writer fails.
     */
    @Throws(IOException::class)
    protected open fun textRaw(out: Appendable, ch: Char) {
        write(out, ch)
    }

    /**
     * Write an [EntityRef] to the destination.
     *
     * @param out
     * the destination Writer.
     * @param name
     * the EntityRef's name.
     * @throws IOException
     * if the Writer fails.
     */
    @Throws(IOException::class)
    protected open fun textEntityRef(out: Appendable, name: String?) {
        textRaw(out, '&')
        textRaw(out, name)
        textRaw(out, ';')
    }

    /**
     * Write a [CDATA] to the destination
     * @param out the destination Writer
     * @param text the CDATA text
     * @throws IOException if the Writer fails.
     */
    @Throws(IOException::class)
    protected open fun textCDATA(out: Appendable, text: String?) {
        textRaw(out, CDATAPRE)
        textRaw(out, text)
        textRaw(out, CDATAPOST)
    }

    /* *******************************************
     * Support methods for output. Should all be protected. All content-type
     * print methods have a FormatStack. Only printContent is responsible for
     * outputting appropriate indenting and newlines, which are easily available
     * using the FormatStack.getLevelIndent() and FormatStack.getLevelEOL().
     * *******************************************
     */

    /**
     * This will handle printing of a [Document].
     *
     * @param out
     * `Writer` to use.
     * @param fstack
     * the FormatStack
     * @param nstack
     * the NamespaceStack
     * @param doc
     * `Document` to write.
     * @throws IOException
     * if the destination Writer fails
     */
    @Throws(IOException::class)
    protected open fun printDocument(
        out: Appendable, fstack: FormatStack,
        nstack: NamespaceStack, doc: Document
    ) {

        // If there is no root element then we cannot use the normal ways to
        // access the ContentList because Document throws an exception.
        // so we hack it and just access it by index.
        val list: MutableList<Content> =
            if (doc.hasRootElement()) doc.content else ArrayList<Content>(doc.contentSize)
        if (list.isEmpty()) {
            val sz = doc.contentSize
            for (i in 0 until sz) {
                list.add(doc.getContent(i))
            }
        }

        printDeclaration(out, fstack)

        val walker = buildWalker(fstack, list, true)
        if (walker.hasNext()) {
            while (walker.hasNext()) {

                val c = walker.next()
                // we do not ignore Text-like things in the Document.
                // the walker creates the indenting for us.
                if (c == null) {
                    // but, what we do is ensure it is all whitespace, and not CDATA
                    val padding = walker.text()
                    if (padding != null && Verifier.isAllXMLWhitespace(padding) &&
                        !walker.isCDATA
                    ) {
                        // we do not use the escaping or text* method because this
                        // content is outside of the root element, and thus is not
                        // strict text.
                        write(out, padding)
                    }
                } else {
                    when (c.cType) {
                        Content.CType.Comment -> printComment(out, fstack, c as Comment)
                        Content.CType.DocType -> printDocType(out, fstack, c as DocType)
                        Content.CType.Element -> printElement(out, fstack, nstack, c as Element)
                        Content.CType.ProcessingInstruction ->
                            printProcessingInstruction(out, fstack, c as ProcessingInstruction)
                        Content.CType.Text -> {
                            val padding = (c as Text).text
                            if (padding != null && Verifier.isAllXMLWhitespace(padding)) {
                                // we do not use the escaping or text* method because this
                                // content is outside of the root element, and thus is not
                                // strict text.
                                write(out, padding)
                            }
                        }
                        else -> {
                            // do nothing.
                        }
                    }
                }
            }

            if (fstack.lineSeparator != null) {
                write(out, fstack.lineSeparator)
            }
        }
    }

    /**
     * This will handle printing of the XML declaration. Assumes XML version 1.0
     * since we don't directly know.
     *
     * @param out
     * `Writer` to use.
     * @param fstack
     * the FormatStack
     * @throws IOException
     * if the destination Writer fails
     */
    @Throws(IOException::class)
    protected open fun printDeclaration(out: Appendable, fstack: FormatStack) {

        // Only print the declaration if it's not being omitted
        if (fstack.isOmitDeclaration) {
            return
        }
        // Declaration is never indented.
        // write(out, fstack.getLevelIndent());

        // Assume 1.0 version
        if (fstack.isOmitEncoding) {
            write(out, "<?xml version=\"1.0\"?>")
        } else {
            write(out, "<?xml version=\"1.0\"")
            write(out, " encoding=\"")
            write(out, fstack.encoding)
            write(out, "\"?>")
        }

        // Print new line after decl always, even if no other new lines
        // Helps the output look better and is semantically
        // inconsequential
        // newline(out, fstack);
        write(out, fstack.lineSeparator)
    }

    /**
     * This will handle printing of a [DocType].
     *
     * @param out
     * `Writer` to use.
     * @param fstack
     * the FormatStack
     * @param docType
     * `DocType` to write.
     * @throws IOException
     * if the destination Writer fails
     */
    @Throws(IOException::class)
    protected open fun printDocType(
        out: Appendable, fstack: FormatStack,
        docType: DocType
    ) {

        val publicID = docType.publicID
        val systemID = docType.systemID
        val internalSubset = docType.internalSubset
        var hasPublic = false

        // Declaration is never indented.
        // write(out, fstack.getLevelIndent());

        write(out, "<!DOCTYPE ")
        write(out, docType.elementName)
        if (publicID != null) {
            write(out, " PUBLIC \"")
            write(out, publicID)
            write(out, "\"")
            hasPublic = true
        }
        if (systemID != null) {
            if (!hasPublic) {
                write(out, " SYSTEM")
            }
            write(out, " \"")
            write(out, systemID)
            write(out, "\"")
        }
        if ((internalSubset != null) && (internalSubset != "")) {
            write(out, " [")
            write(out, fstack.lineSeparator)
            write(out, docType.internalSubset)
            write(out, "]")
        }
        write(out, ">")
    }

    /**
     * This will handle printing of a [ProcessingInstruction].
     *
     * @param out
     * `Writer` to use.
     * @param fstack
     * the FormatStack
     * @param pi
     * `ProcessingInstruction` to write.
     * @throws IOException
     * if the destination Writer fails
     */
    @Throws(IOException::class)
    protected open fun printProcessingInstruction(
        out: Appendable,
        fstack: FormatStack, pi: ProcessingInstruction
    ) {
        val target = pi.target
        var piProcessed = false

        if (fstack.isIgnoreTrAXEscapingPIs == false) {
            if (target == PI_DISABLE_OUTPUT_ESCAPING) {
                // special case... change the FormatStack
                fstack.escapeOutput = false
                piProcessed = true
            } else if (target == PI_ENABLE_OUTPUT_ESCAPING) {
                // special case... change the FormatStack
                fstack.escapeOutput = true
                piProcessed = true
            }
        }
        if (piProcessed == false) {
            val rawData = pi.data

            // Write <?target data?> or if no data then just <?target?>
            if ("" != rawData) {
                write(out, "<?")
                write(out, target)
                write(out, " ")
                write(out, rawData)
                write(out, "?>")
            } else {
                write(out, "<?")
                write(out, target)
                write(out, "?>")
            }
        }
    }

    /**
     * This will handle printing of a [Comment].
     *
     * @param out
     * `Writer` to use.
     * @param fstack
     * the FormatStack
     * @param comment
     * `Comment` to write.
     * @throws IOException
     * if the destination Writer fails
     */
    @Throws(IOException::class)
    protected open fun printComment(
        out: Appendable, fstack: FormatStack,
        comment: Comment
    ) {
        write(out, "<!--")
        write(out, comment.text)
        write(out, "-->")
    }

    /**
     * This will handle printing of an [EntityRef].
     *
     * @param out
     * `Writer` to use.
     * @param fstack
     * the FormatStack
     * @param entity
     * `EntotyRef` to write.
     * @throws IOException
     * if the destination Writer fails
     */
    @Throws(IOException::class)
    protected open fun printEntityRef(
        out: Appendable, fstack: FormatStack,
        entity: EntityRef
    ) {
        // EntityRefs are treated like text, not indented/newline content.
        textEntityRef(out, entity.name)
    }

    /**
     * This will handle printing of a [CDATA].
     *
     * @param out
     * `Writer` to use.
     * @param fstack
     * the FormatStack
     * @param cdata
     * `CDATA` to write.
     * @throws IOException
     * if the destination Writer fails
     */
    @Throws(IOException::class)
    protected open fun printCDATA(
        out: Appendable, fstack: FormatStack,
        cdata: CDATA
    ) {
        // CDATAs are treated like text, not indented/newline content.
        textCDATA(out, cdata.text)
    }

    /**
     * This will handle printing of a [Text].
     *
     * @param out
     * `Writer` to use.
     * @param fstack
     * the FormatStack
     * @param text
     * `Text` to write.
     * @throws IOException
     * if the destination Writer fails
     */
    @Throws(IOException::class)
    protected open fun printText(
        out: Appendable, fstack: FormatStack,
        text: Text
    ) {
        if (fstack.escapeOutput) {
            textRaw(
                out, Format.escapeText(
                    fstack.escapeStrategy,
                    fstack.lineSeparator, text.text
                )
            )

            return
        }
        textRaw(out, text.text)
    }

    /**
     * This will handle printing of an [Element].
     *
     * This method arranges for outputting the Element infrastructure including
     * Namespace Declarations and Attributes.
     *
     * @param out
     * `Writer` to use.
     * @param fstack
     * the FormatStack
     * @param nstack
     * the NamespaceStack
     * @param element
     * `Element` to write.
     * @throws IOException
     * if the destination Writer fails
     */
    @Throws(IOException::class)
    protected open fun printElement(
        out: Appendable, fstack: FormatStack,
        nstack: NamespaceStack, element: Element
    ) {

        nstack.push(element)
        try {
            val content = element.content

            // Print the beginning of the tag plus attributes and any
            // necessary namespace declarations
            write(out, "<")

            write(out, element.qualifiedName)

            // Print the element's namespace, if appropriate
            for (ns in nstack.addedForward()) {
                printNamespace(out, fstack, ns)
            }

            // Print out attributes
            if (element.hasAttributes()) {
                for (attribute in element.attributes) {
                    printAttribute(out, fstack, attribute)
                }
            }

            if (content.isEmpty()) {
                // Case content is empty
                if (fstack.isExpandEmptyElements) {
                    write(out, "></")
                    write(out, element.qualifiedName)
                    write(out, ">")
                } else {
                    write(out, " />")
                }
                // nothing more to do.
                return
            }

            // OK, we have real content to push.
            fstack.push()
            try {

                // Check for xml:space and adjust format settings
                val space = element.getAttributeValue(
                    "space",
                    Namespace.XML_NAMESPACE
                )

                if ("default" == space) {
                    fstack.textMode = fstack.defaultMode
                } else if ("preserve" == space) {
                    fstack.textMode = TextMode.PRESERVE
                }

                // note we ensure the FStack is right before creating the walker
                val walker = buildWalker(fstack, content, true)

                if (!walker.hasNext()) {
                    // the walker has formatted out whatever content we had
                    if (fstack.isExpandEmptyElements) {
                        write(out, "></")
                        write(out, element.qualifiedName)
                        write(out, ">")
                    } else {
                        write(out, " />")
                    }
                    // nothing more to do.
                    return
                }
                // we have some content.
                write(out, ">")
                if (!walker.isAllText) {
                    // we need to newline/indent
                    textRaw(out, fstack.padBetween)
                }

                printContent(out, fstack, nstack, walker)

                if (!walker.isAllText) {
                    // we need to newline/indent
                    textRaw(out, fstack.padLast)
                }
                write(out, "</")
                write(out, element.qualifiedName)
                write(out, ">")
            } finally {
                fstack.pop()
            }
        } finally {
            nstack.pop()
        }
    }

    /**
     * This will handle printing of a List of [Content].
     *
     * The list of Content is basically processed as one of three types of
     * content
     *  1. Consecutive text-type (Text, CDATA, and EntityRef) content
     *  2. Stand-alone text-type content
     *  3. Non-text-type content.
     *
     * Although the code looks complex, the theory is conceptually simple:
     *  1. identify one of the three types (consecutive, stand-alone, non-text)
     *  2. do indent if any is specified.
     *  3. send the type to the respective print* handler (e.g.
     * [printCDATA], or
     * [printComment],
     *  4. do a newline if one is specified.
     *  5. loop back to 1. until there's no more content to process.
     *
     * @param out
     * `Writer` to use.
     * @param fstack
     * the FormatStack
     * @param nstack
     * the NamespaceStack
     * @param walker
     * [Walker] of `Content` to write.
     * @throws IOException
     * if the destination Writer fails
     */
    @Throws(IOException::class)
    protected open fun printContent(
        out: Appendable,
        fstack: FormatStack, nstack: NamespaceStack,
        walker: Walker
    ) {

        while (walker.hasNext()) {
            val c = walker.next()
            if (c == null) {
                // it is a text value of some sort.
                val t = walker.text()
                if (walker.isCDATA) {
                    textCDATA(out, t)
                } else {
                    textRaw(out, t)
                }
            } else {
                when (c.cType) {
                    Content.CType.CDATA -> printCDATA(out, fstack, c as CDATA)
                    Content.CType.Comment -> printComment(out, fstack, c as Comment)
                    Content.CType.DocType -> printDocType(out, fstack, c as DocType)
                    Content.CType.Element -> printElement(out, fstack, nstack, c as Element)
                    Content.CType.EntityRef -> printEntityRef(out, fstack, c as EntityRef)
                    Content.CType.ProcessingInstruction ->
                        printProcessingInstruction(out, fstack, c as ProcessingInstruction)
                    Content.CType.Text -> printText(out, fstack, c as Text)
                }
            }
        }
    }

    /**
     * This will handle printing of any needed `[Namespace]`
     * declarations.
     *
     * @param out
     * `Writer` to use.
     * @param fstack
     * The current FormatStack
     * @param ns
     * `Namespace` to print definition of
     * @throws IOException
     * if the output fails
     */
    @Throws(IOException::class)
    protected open fun printNamespace(
        out: Appendable, fstack: FormatStack,
        ns: Namespace
    ) {
        val prefix = ns.prefix
        val uri = ns.uri

        write(out, " xmlns")
        if (prefix != "") {
            write(out, ":")
            write(out, prefix)
        }
        write(out, "=\"")
        attributeEscapedEntitiesFilter(out, fstack, uri)
        write(out, "\"")
    }

    /**
     * This will handle printing of an `[Attribute]`.
     *
     * @param out
     * `Writer` to use.
     * @param fstack
     * The current FormatStack
     * @param attribute
     * `Attribute` to output
     * @throws IOException
     * if the output fails
     */
    @Throws(IOException::class)
    protected open fun printAttribute(
        out: Appendable, fstack: FormatStack,
        attribute: Attribute
    ) {

        if (!attribute.isSpecified && fstack.isSpecifiedAttributesOnly) {
            return
        }
        write(out, " ")
        write(out, attribute.qualifiedName)
        write(out, "=")

        write(out, "\"")
        attributeEscapedEntitiesFilter(out, fstack, attribute.value)
        write(out, "\"")
    }

    companion object {
        /** Simple constant for an open-CDATA  */
        protected const val CDATAPRE = "<![CDATA["

        /** Simple constant for a close-CDATA  */
        protected const val CDATAPOST = "]]>"

        /** `javax.xml.transform.Result.PI_DISABLE_OUTPUT_ESCAPING` (the JAXP constant's value, no JAXP in common code). */
        private const val PI_DISABLE_OUTPUT_ESCAPING = "javax.xml.transform.disable-output-escaping"

        /** `javax.xml.transform.Result.PI_ENABLE_OUTPUT_ESCAPING` (the JAXP constant's value). */
        private const val PI_ENABLE_OUTPUT_ESCAPING = "javax.xml.transform.enable-output-escaping"
    }
}

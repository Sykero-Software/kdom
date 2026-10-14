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

import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Content
import fi.sykero.kdom.DocType
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.EntityRef
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer

/*
 * JVM-only `XMLOutputter.output(.., OutputStream)` overloads. In jdom2 they were members of XMLOutputter; kdom's
 * common XMLOutputter writes to an `Appendable`, so the byte-stream entry points are JVM extension functions
 * (Java callers: `XMLOutputterJvmKt.output(outputter, doc, stream)`). They encode with `Format.encoding` and
 * defer to the `Appendable` overloads, which flush the writer, exactly as jdom2 did.
 */

/**
 * This will print the Document to the given OutputStream. The characters are
 * printed using the encoding specified in the constructor, or a default of UTF-8.
 *
 * @param doc the content to output.
 * @param out `OutputStream` to use.
 * @throws IOException if there's any problem writing.
 * @throws NullPointerException if the specified content is null.
 */
@Throws(IOException::class)
fun XMLOutputter.output(doc: Document, out: OutputStream) {
    output(doc, makeWriter(out, format))
}

/**
 * This will print the DocType to the given OutputStream. The characters are
 * printed using the encoding specified in the constructor, or a default of UTF-8.
 *
 * @param doctype the content to output.
 * @param out `OutputStream` to use.
 * @throws IOException if there's any problem writing.
 * @throws NullPointerException if the specified content is null.
 */
@Throws(IOException::class)
fun XMLOutputter.output(doctype: DocType, out: OutputStream) {
    output(doctype, makeWriter(out, format))
}

/**
 * This will print the Element to the given OutputStream. The characters are
 * printed using the encoding specified in the constructor, or a default of UTF-8.
 *
 * @param element the content to output.
 * @param out `OutputStream` to use.
 * @throws IOException if there's any problem writing.
 * @throws NullPointerException if the specified content is null.
 */
@Throws(IOException::class)
fun XMLOutputter.output(element: Element, out: OutputStream) {
    output(element, makeWriter(out, format))
}

/**
 * This will handle printing out an [Element]'s content only, not including its tag, and attributes.
 * This can be useful for printing the content of an element that contains
 * HTML, like "&lt;description&gt;JDOM is &lt;b&gt;fun&gt;!&lt;/description&gt;".
 *
 * @param element `Element` to output.
 * @param out `OutputStream` to use.
 * @throws IOException if there's any problem writing.
 * @throws NullPointerException if the specified content is null.
 */
@Throws(IOException::class)
fun XMLOutputter.outputElementContent(element: Element, out: OutputStream) {
    outputElementContent(element, makeWriter(out, format))
}

/**
 * This will print the list of Content to the given OutputStream. The characters are
 * printed using the encoding specified in the constructor, or a default of UTF-8.
 *
 * @param list the content to output.
 * @param out `OutputStream` to use.
 * @throws IOException if there's any problem writing.
 * @throws NullPointerException if the specified content is null.
 */
@Throws(IOException::class)
fun XMLOutputter.output(list: List<Content>, out: OutputStream) {
    output(list, makeWriter(out, format))
}

/**
 * This will print the CDATA to the given OutputStream. The characters are
 * printed using the encoding specified in the constructor, or a default of UTF-8.
 *
 * @param cdata the content to output.
 * @param out `OutputStream` to use.
 * @throws IOException if there's any problem writing.
 * @throws NullPointerException if the specified content is null.
 */
@Throws(IOException::class)
fun XMLOutputter.output(cdata: CDATA, out: OutputStream) {
    output(cdata, makeWriter(out, format))
}

/**
 * This will print the Text to the given OutputStream. The characters are
 * printed using the encoding specified in the constructor, or a default of UTF-8.
 *
 * @param text the content to output.
 * @param out `OutputStream` to use.
 * @throws IOException if there's any problem writing.
 * @throws NullPointerException if the specified content is null.
 */
@Throws(IOException::class)
fun XMLOutputter.output(text: Text, out: OutputStream) {
    output(text, makeWriter(out, format))
}

/**
 * This will print the Comment to the given OutputStream. The characters are
 * printed using the encoding specified in the constructor, or a default of UTF-8.
 *
 * @param comment the content to output.
 * @param out `OutputStream` to use.
 * @throws IOException if there's any problem writing.
 * @throws NullPointerException if the specified content is null.
 */
@Throws(IOException::class)
fun XMLOutputter.output(comment: Comment, out: OutputStream) {
    output(comment, makeWriter(out, format))
}

/**
 * This will print the ProcessingInstruction to the given OutputStream. The characters are
 * printed using the encoding specified in the constructor, or a default of UTF-8.
 *
 * @param pi the content to output.
 * @param out `OutputStream` to use.
 * @throws IOException if there's any problem writing.
 * @throws NullPointerException if the specified content is null.
 */
@Throws(IOException::class)
fun XMLOutputter.output(pi: ProcessingInstruction, out: OutputStream) {
    output(pi, makeWriter(out, format))
}

/**
 * This will print the EntityRef to the given OutputStream. The characters are
 * printed using the encoding specified in the constructor, or a default of UTF-8.
 *
 * @param entity the content to output.
 * @param out `OutputStream` to use.
 * @throws IOException if there's any problem writing.
 * @throws NullPointerException if the specified content is null.
 */
@Throws(IOException::class)
fun XMLOutputter.output(entity: EntityRef, out: OutputStream) {
    output(entity, makeWriter(out, format))
}

/**
 * Get an OutputStreamWriter, use specified encoding.
 *
 * @param out
 * The OutputStream to wrap in the writer
 * @param format
 * The format is used to obtain the Character Encoding.
 * @return An Writer (Buffered) that delegates to the specified output steam
 * @throws java.io.UnsupportedEncodingException
 */
@Throws(java.io.UnsupportedEncodingException::class)
private fun makeWriter(out: OutputStream, format: Format): Writer {
    return BufferedWriter(
        OutputStreamWriter(
            BufferedOutputStream(out), format.encoding
        )
    )
}

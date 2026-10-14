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

 Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software.

 */
package fi.sykero.kdom.output

import fi.sykero.kdom.IllegalDataException
import fi.sykero.kdom.Verifier

/**
 * Class to encapsulate XMLOutputter format options.
 * Typically users adapt the standard format configurations obtained by
 * [getRawFormat] (no whitespace changes),
 * [getPrettyFormat] (whitespace beautification), and
 * [getCompactFormat] (whitespace normalization).
 *
 * Several modes are available to effect the way textual content is printed.
 * See the documentation for [TextMode] for details.
 *
 * **Note about Line Separator:**
 *
 * By default JDOM will always use the CRNL sequence "\r\n" for output. This
 * can be changed in a number of different ways. See the [LineSeparator]
 * enumeration for more information.
 *
 * **Note about XML Character Escaping:**
 *
 * JDOM will escape characters in the output based on the EscapeStrategy that
 * is specified by this Format. The Format will by default use a sensible
 * EscapeStrategy that is based on the character encoding of the output. If
 * the default escape mechanism is not producing the correct results you can
 * change the EscapeStrategy on the format to suit your own needs.
 *
 * @see LineSeparator
 *
 * @author Jason Hunter
 * @author Rolf Lear
 */
open class Format private constructor() {

    /**
     * An EscapeStrategy suitable for UTF-8 an UTF-16. We want the class to
     * have its own name.
     */
    private class EscapeStrategyUTF : EscapeStrategy {
        override fun shouldEscape(ch: Char): Boolean {
            return Verifier.isHighSurrogate(ch)
        }
    }

    /**
     * An EscapeStrategy suitable for 8-bit charsets. We want the class to have
     * its own name.
     */
    private class EscapeStrategy8Bits : EscapeStrategy {
        override fun shouldEscape(ch: Char): Boolean {
            return (ch.code ushr 8) != 0
        }
    }

    /**
     * An EscapeStrategy suitable for 7-bit charsets. We want the class to
     * have its own name.
     */
    private class EscapeStrategy7Bits : EscapeStrategy {
        override fun shouldEscape(ch: Char): Boolean {
            return (ch.code ushr 7) != 0
        }
    }

    /** The default indent is no spaces (as original document) */
    var indent: String? = null
        @JvmSynthetic
        set(value) {
            field = value
        }

    /** New line separator */
    var lineSeparator: String? = STANDARD_LINE_SEPARATOR
        @JvmSynthetic
        set(value) {
            field = if ("" == value) null else value
        }

    /** The encoding format */
    var encoding: String = STANDARD_ENCODING
        @JvmSynthetic
        set(value) {
            field = value
            escapeStrategy = chooseStrategy(value)
        }

    /** Whether or not to output the XML declaration
     * - default is `false` */
    var omitDeclaration: Boolean = false
        @JvmSynthetic
        set(value) {
            field = value
        }

    /** Whether or not to output the encoding in the XML declaration
     * - default is `false` */
    var omitEncoding: Boolean = false
        @JvmSynthetic
        set(value) {
            field = value
        }

    /** Whether Attributes that are defaulted from the DTD or Schema
     * are output.
     *
     * Will Attributes defaulted from the DTD or XMLSchema be output: `true` if
     * only the specified Attributes will be output.
     * Set whether only those Attributes specified in the input XML should
     * be output. Other Attributes (those defaulted or 'fixed' in the DTD
     * or XMLSchema) should be ignored. */
    var isSpecifiedAttributesOnly: Boolean = false

    /** Whether or not to expand empty elements to
     * `<tagName></tagName>` - default is `false` */
    var expandEmptyElements: Boolean = false
        @JvmSynthetic
        set(value) {
            field = value
        }

    /**
     * Whether JAXP TrAX processing instructions for
     * disabling/enabling output escaping are ignored.  Disabling
     * output escaping allows using XML text as element content and
     * outputting it verbatim, i.e. as element children would be.
     *
     * When processed, these processing instructions are removed from
     * the generated XML text and control whether the element text
     * content is output verbatim or with escaping of the pre-defined
     * entities in XML 1.0.  The text to be output verbatim shall be
     * surrounded by the
     * `<?javax.xml.transform.disable-output-escaping ?>`
     * and `<?javax.xml.transform.enable-output-escaping ?>`
     * PIs.
     *
     * When ignored, the processing instructions are present in the
     * generated XML text and the pre-defined entities in XML 1.0 are
     * escaped.
     *
     * Default: `false`.
     */
    var ignoreTrAXEscapingPIs: Boolean = false

    /**
     * The text output style.  Options are available as
     * [TextMode] instances.  The default is [TextMode.PRESERVE].
     */
    var textMode: TextMode = TextMode.PRESERVE
        @JvmSynthetic
        set(value) {
            field = value
        }

    /**
     * The current escape strategy, used for character escaping.
     */
    var escapeStrategy: EscapeStrategy = DefaultEscapeStrategy
        @JvmSynthetic
        set(value) {
            field = value
        }

    init {
        // creates a new Format instance with default (raw) behavior.
        encoding = STANDARD_ENCODING
    }

    /**
     * Sets the [EscapeStrategy] to use for character escaping.
     *
     * @param strategy the EscapeStrategy to use
     * @return a pointer to this Format for chaining
     */
    open fun setEscapeStrategy(strategy: EscapeStrategy): Format {
        escapeStrategy = strategy
        return this
    }

    /**
     * This will set the newline separator (`LineSeparator`).
     * The default is `\r\n`.
     *
     * Use the [setLineSeparator] method taking a [LineSeparator] to set
     * standard separators in an easier way.
     *
     * To make it output the system default line ending string, call
     * `setLineSeparator(System.getProperty("line.separator"))`.
     *
     * To output "UNIX-style" documents, call
     * `setLineSeparator("\n")`.  To output "Mac-style"
     * documents, call `setLineSeparator("\r")`.  DOS-style
     * documents use CR-LF ("\r\n"), which is the default.
     *
     * Note that this only applies to newlines generated by the
     * outputter.  All XML parsers are required to 'normalize' all the
     * combinations of line separators to just '\n'. As a result, if any JDOM
     * component has an end-of-line-like value (e.g. '\r') in it then that value
     * must be the result of an escaped value in the XML source document
     * `&#xD;` or a value explicitly set with one of the Text
     * value setters. Values in JDOM content that were explicitly set to be
     * '\r' will always be escaped on XML Output.
     *
     * The actual newline separator itself though can be set with this method.
     * Any internal newlines in Text output will be represented by this
     * end-of-line sequence.
     *
     * If the format's "indent" property is null (as is the default
     * for the Raw and Compact formats), then this value only effects the
     * newlines written after the declaration and doctype, as well as any
     * newlines embedded within existing text content.
     *
     * Setting the indent to be null will disable end-of-line processing
     * for any formatting, but will not affect substitution of embedded \n.
     * Setting this value to null or the empty string will disable all
     * end-of-line modifications.
     *
     * @see setTextMode
     * @see setLineSeparator
     *
     * @param separator `String` line separator to use.
     * @return a pointer to this Format for chaining
     */
    open fun setLineSeparator(separator: String?): Format {
        lineSeparator = separator
        return this
    }

    /**
     * This will set the newline separator sequence.
     *
     * This method differs from [setLineSeparator] taking a String slightly in
     * that, to disable end-of-line processing you should call:
     * ```
     * Format.setLinewSeparator(LineSeparator.NONE);
     * ```
     *
     * @param separator [LineSeparator] line separator to us
     * @return a pointer to this Format for chaining
     * @since JDOM2
     */
    open fun setLineSeparator(separator: LineSeparator?): Format {
        return setLineSeparator(
            if (separator == null) STANDARD_LINE_SEPARATOR else separator.value()
        )
    }

    /**
     * This will set whether the XML declaration
     * (`<?xml version="1.0" encoding="UTF-8"?>`)
     * includes the encoding of the document. It is common to omit
     * this in uses such as WML and other wireless device protocols.
     *
     * @param omitEncoding `boolean` indicating whether or not
     * the XML declaration should indicate the document encoding.
     * @return a pointer to this Format for chaining
     */
    open fun setOmitEncoding(omitEncoding: Boolean): Format {
        this.omitEncoding = omitEncoding
        return this
    }

    /**
     * This will set whether the XML declaration
     * (`<?xml version="1.0"?>`)
     * will be omitted or not. It is common to omit this in uses such
     * as SOAP and XML-RPC calls.
     *
     * @param omitDeclaration `boolean` indicating whether or not
     * the XML declaration should be omitted.
     * @return a pointer to this Format for chaining
     */
    open fun setOmitDeclaration(omitDeclaration: Boolean): Format {
        this.omitDeclaration = omitDeclaration
        return this
    }

    /**
     * This will set whether empty elements are expanded from
     * `<tagName/>` to
     * `<tagName></tagName>`.
     *
     * @param expandEmptyElements `boolean` indicating whether or not
     * empty elements should be expanded.
     * @return a pointer to this Format for chaining
     */
    open fun setExpandEmptyElements(expandEmptyElements: Boolean): Format {
        this.expandEmptyElements = expandEmptyElements
        return this
    }

    /**
     * This sets the text output style.  Options are available as
     * [TextMode] instances.  The default is [TextMode.PRESERVE].
     *
     * @param mode The TextMode to set.
     * @return a pointer to this Format for chaining
     */
    open fun setTextMode(mode: TextMode): Format {
        this.textMode = mode
        return this
    }

    /**
     * This will set the indent `String` to use; this
     * is usually a `String` of empty spaces. If you pass
     * the empty string (""), then no indentation will happen but newlines
     * will still be generated.  Passing null will result in no indentation
     * and no newlines generated.  Default: none (null)
     *
     * @param indent `String` to use for indentation.
     * @return a pointer to this Format for chaining
     */
    open fun setIndent(indent: String?): Format {
        this.indent = indent
        return this
    }

    /**
     * Sets the output encoding.  The name should be an accepted XML
     * encoding.
     *
     * @param encoding the encoding format.  Use XML-style names like
     * "UTF-8" or "ISO-8859-1" or "US-ASCII"
     * @return a pointer to this Format for chaining
     */
    open fun setEncoding(encoding: String): Format {
        this.encoding = encoding
        return this
    }

    /**
     * Returns a copy of this Format (jdom2: `Object.clone()`, a field-by-field copy; every field is an immutable
     * value or a shared [EscapeStrategy]).
     */
    open fun clone(): Format {
        val copy = Format()
        copy.indent = indent
        copy.lineSeparator = lineSeparator
        copy.encoding = encoding // re-chooses the strategy; the copied strategy below wins, as in jdom2
        copy.omitDeclaration = omitDeclaration
        copy.omitEncoding = omitEncoding
        copy.isSpecifiedAttributesOnly = isSpecifiedAttributesOnly
        copy.expandEmptyElements = expandEmptyElements
        copy.ignoreTrAXEscapingPIs = ignoreTrAXEscapingPIs
        copy.textMode = textMode
        copy.escapeStrategy = escapeStrategy
        return copy
    }

    /**
     * Class to signify how text should be handled on output.  The following
     * table provides details.
     *
     * | Text Mode | Resulting behavior. |
     * |---|---|
     * | *PRESERVE (Default)* | All content is printed in the format it was created, no whitespace or line separators are are added or removed. |
     * | TRIM_FULL_WHITE | Content between tags consisting of all whitespace is not printed. If the content contains even one non-whitespace character, it is all printed verbatim, whitespace and all. |
     * | TRIM | All leading and trailing whitespace is trimmed. |
     * | NORMALIZE | Leading and trailing whitespace is trimmed, and any 'internal' whitespace is compressed to a single space. |
     *
     * In most cases textual content is aligned with the surrounding tags
     * (after the appropriate text mode is applied). In the case where the only
     * content between the start and end tags is textual, the start tag, text,
     * and end tag are all printed on the same line. If the document being
     * output already has whitespace, it's wise to turn on TRIM mode so the
     * pre-existing whitespace can be trimmed before adding new whitespace.
     *
     * When an element has a xml:space attribute with the value of "preserve",
     * all formatting is turned off (actually, the TextMode is set to
     * [PRESERVE] until the element and its contents have been printed.
     * If a nested element contains another xml:space with the value "default"
     * formatting is turned back on  for the child element and then off for the
     * remainder of the parent element.
     *
     * @since JDOM2
     */
    enum class TextMode {
        /**
         * Mode for literal text preservation.
         */
        PRESERVE,

        /**
         * Mode for text trimming (left and right trim).
         */
        TRIM,

        /**
         * Mode for text normalization (left and right trim plus internal
         * whitespace is normalized to a single space.
         * @see fi.sykero.kdom.Element.textNormalize
         */
        NORMALIZE,

        /**
         * Mode for text trimming of content consisting of nothing but
         * whitespace but otherwise not changing output.
         */
        TRIM_FULL_WHITE
    }

    companion object {

        /**
         * An EscapeStrategy suitable for UTF-8 an UTF-16
         */
        private val UTFEscapeStrategy: EscapeStrategy = EscapeStrategyUTF()

        /**
         * An EscapeStrategy suitable for 8-bit charsets
         */
        private val Bits8EscapeStrategy: EscapeStrategy = EscapeStrategy8Bits()

        /**
         * An EscapeStrategy suitable for 7-bit charsets
         */
        private val Bits7EscapeStrategy: EscapeStrategy = EscapeStrategy7Bits()

        /**
         * An EscapeStrategy suitable for 'unknown' charsets (the initial value of [escapeStrategy], replaced by the
         * constructor's `encoding = STANDARD_ENCODING`).
         */
        private val DefaultEscapeStrategy: EscapeStrategy = object : EscapeStrategy {
            override fun shouldEscape(ch: Char): Boolean {
                if (Verifier.isHighSurrogate(ch)) {
                    return true // Safer this way per http://unicode.org/faq/utf_bom.html#utf8-4
                }

                return false
            }
        }

        /** standard value to indent by, if we are indenting */
        private const val STANDARD_INDENT = "  "

        /** standard string with which to end a line */
        private val STANDARD_LINE_SEPARATOR: String? = LineSeparator.DEFAULT.value()

        /** standard encoding */
        private const val STANDARD_ENCODING = "UTF-8"

        /**
         * Returns a new Format object that performs no whitespace changes, uses
         * the UTF-8 encoding, doesn't expand empty elements, includes the
         * declaration and encoding, and uses the default entity escape strategy.
         * Tweaks can be made to the returned Format instance without affecting
         * other instances.
         *
         * @return a Format with no whitespace changes
         */
        @JvmStatic
        fun getRawFormat(): Format {
            return Format()
        }

        /**
         * Returns a new Format object that performs whitespace beautification with
         * 2-space indents, uses the UTF-8 encoding, doesn't expand empty elements,
         * includes the declaration and encoding, and uses the default entity
         * escape strategy.
         * Tweaks can be made to the returned Format instance without affecting
         * other instances.
         *
         * @return a Format with whitespace beautification
         */
        @JvmStatic
        fun getPrettyFormat(): Format {
            val f = Format()
            f.setIndent(STANDARD_INDENT)
            f.setTextMode(TextMode.TRIM)
            return f
        }

        /**
         * Returns a new Format object that performs whitespace normalization, uses
         * the UTF-8 encoding, doesn't expand empty elements, includes the
         * declaration and encoding, and uses the default entity escape strategy.
         * Tweaks can be made to the returned Format instance without affecting
         * other instances.
         *
         * @return a Format with whitespace normalization
         */
        @JvmStatic
        fun getCompactFormat(): Format {
            val f = Format()
            f.setTextMode(TextMode.NORMALIZE)
            return f
        }

        /**
         * Use the XML Specification definition of whitespace to compact the
         * input value. The value is trimmed, and any internal XML whitespace
         * is replaced with a single ' ' space.
         * @param str The value to compact.
         * @return The compacted value
         * @since JDOM2
         */
        @JvmStatic
        fun compact(str: String): String {
            var right = str.length - 1
            var left = 0
            while (left <= right &&
                Verifier.isXMLWhitespace(str[left])
            ) {
                left++
            }
            while (right > left &&
                Verifier.isXMLWhitespace(str[right])
            ) {
                right--
            }

            if (left > right) {
                return ""
            }

            var space = true
            val buffer = StringBuilder(right - left + 1)
            while (left <= right) {
                val c = str[left]
                if (Verifier.isXMLWhitespace(c)) {
                    if (space) {
                        buffer.append(' ')
                        space = false
                    }
                } else {
                    buffer.append(c)
                    space = true
                }
                left++
            }
            return buffer.toString()
        }

        /**
         * Use the XML Specification definition of whitespace to Right-trim the
         * input value.
         * @param str The value to trim.
         * @return The value right-trimmed
         * @since JDOM2
         */
        @JvmStatic
        fun trimRight(str: String): String {
            var right = str.length - 1
            while (right >= 0 && Verifier.isXMLWhitespace(str[right])) {
                right--
            }
            if (right < 0) {
                return ""
            }
            return str.substring(0, right + 1)
        }

        /**
         * Use the XML Specification definition of whitespace to Left-trim the
         * input value.
         * @param str The value to trim.
         * @return The value left-trimmed
         * @since JDOM2
         */
        @JvmStatic
        fun trimLeft(str: String): String {
            val right = str.length
            var left = 0
            while (left < right && Verifier.isXMLWhitespace(str[left])) {
                left++
            }
            if (left >= right) {
                return ""
            }

            return str.substring(left)
        }

        /**
         * Use the XML Specification definition of whitespace to trim the
         * input value.
         * @param str The value to trim.
         * @return The value trimmed
         * @since JDOM2
         */
        @JvmStatic
        fun trimBoth(str: String): String {
            var right = str.length - 1
            while (right > 0 && Verifier.isXMLWhitespace(str[right])) {
                right--
            }
            var left = 0
            while (left <= right && Verifier.isXMLWhitespace(str[left])) {
                left++
            }
            if (left > right) {
                return ""
            }
            return str.substring(left, right + 1)
        }

        /**
         * This will take the three pre-defined entities in XML 1.0 ('&lt;', '&gt;',
         * and '&amp;' - used specifically in XML elements) as well as CR/NL, tabs,
         * and Quote characters which require escaping inside Attribute values and
         * converts their character representation to the appropriate entity
         * reference suitable for XML attribute content. Further, some special
         * characters (e.g. characters that are not valid in the current encoding)
         * are converted to escaped representations.
         *
         * @param strategy
         * The EscapeStrategy to query.
         * @param value
         * `String` Attribute value to escape.
         * @return The value appropriately escaped.
         * @throws IllegalDataException
         * if an entity can not be escaped
         */
        @JvmStatic
        fun escapeAttribute(strategy: EscapeStrategy, value: String): String {
            val len = value.length
            var idx = 0

            while (idx < len) {
                val ch = value[idx]
                if (ch == '<' || ch == '>' || ch == '&' || ch == '\r' || ch == '\n' ||
                    ch == '"' || ch == '\t' || strategy.shouldEscape(ch)
                ) {
                    break
                }
                idx++
            }

            if (idx == len) {
                return value
            }

            var highsurrogate = 0.toChar()
            val sb = StringBuilder(len + 5)
            sb.append(value, 0, idx)
            while (idx < len) {
                val ch = value[idx++]
                if (highsurrogate.code > 0) {
                    if (!Verifier.isLowSurrogate(ch)) {
                        throw IllegalDataException(
                            "Could not decode surrogate pair 0x" +
                                Integer.toHexString(highsurrogate.code) + " / 0x" +
                                Integer.toHexString(ch.code)
                        )
                    }
                    val chp = Verifier.decodeSurrogatePair(highsurrogate, ch)
                    sb.append("&#x")
                    sb.append(Integer.toHexString(chp))
                    sb.append(';')
                    highsurrogate = 0.toChar()
                    continue
                }
                when (ch) {
                    '<' -> sb.append("&lt;")
                    '>' -> sb.append("&gt;")
                    '&' -> sb.append("&amp;")
                    '\r' -> sb.append("&#xD;")
                    '"' -> sb.append("&quot;")
                    '\t' -> sb.append("&#x9;")
                    '\n' -> sb.append("&#xA;")
                    else ->
                        if (strategy.shouldEscape(ch)) {
                            // make sure what we are escaping is not the
                            // beginning of a multi-byte character.
                            if (Verifier.isHighSurrogate(ch)) {
                                // this is a the high of a surrogate pair
                                highsurrogate = ch
                            } else {
                                sb.append("&#x")
                                sb.append(Integer.toHexString(ch.code))
                                sb.append(';')
                            }
                        } else {
                            sb.append(ch)
                        }
                }
            }
            if (highsurrogate.code > 0) {
                throw IllegalDataException(
                    "Surrogate pair 0x" +
                        Integer.toHexString(highsurrogate.code) + "truncated"
                )
            }

            return sb.toString()
        }

        /**
         * This will take the three pre-defined entities in XML 1.0 ('&lt;', '&gt;',
         * and '&amp;' - used specifically in XML elements) and convert their
         * character representation to the appropriate entity reference, suitable
         * for XML element content. Further, some special characters (e.g.
         * characters that are not valid in the current encoding) are converted to
         * escaped representations. If the eol parameter is not null, then any
         * internal newlines will be replaced with the specified eol sequence.
         *
         * @param strategy
         * The EscapeStrategy
         * @param eol
         * The End-Of-Line sequence to be used (may be null).
         * @param value
         * The String to escape
         * @return The input value escaped.
         * @throws IllegalDataException
         * if an entity can not be escaped
         * @since JDOM2
         */
        @JvmStatic
        fun escapeText(strategy: EscapeStrategy, eol: String?, value: String): String {
            val right = value.length
            var idx = 0
            while (idx < right) {
                val ch = value[idx]
                if (ch == '<' || ch == '>' || ch == '&' || ch == '\r' || ch == '\n' ||
                    strategy.shouldEscape(ch)
                ) {
                    break
                }
                idx++
            }

            if (idx == right) {
                // no escape needed.
                return value
            }

            val sb = StringBuilder()
            if (idx > 0) {
                sb.append(value, 0, idx)
            }
            var highsurrogate = 0.toChar()
            while (idx < right) {
                val ch = value[idx++]
                if (highsurrogate.code > 0) {
                    if (!Verifier.isLowSurrogate(ch)) {
                        throw IllegalDataException(
                            "Could not decode surrogate pair 0x" +
                                Integer.toHexString(highsurrogate.code) + " / 0x" +
                                Integer.toHexString(ch.code)
                        )
                    }
                    val chp = Verifier.decodeSurrogatePair(highsurrogate, ch)
                    sb.append("&#x" + Integer.toHexString(chp) + ";")
                    highsurrogate = 0.toChar()
                    continue
                }
                when (ch) {
                    '<' -> sb.append("&lt;")
                    '>' -> sb.append("&gt;")
                    '&' -> sb.append("&amp;")
                    '\r' -> sb.append("&#xD;")
                    '\n' ->
                        if (eol != null) {
                            sb.append(eol)
                        } else {
                            sb.append('\n')
                        }
                    else ->
                        if (strategy.shouldEscape(ch)) {
                            // make sure what we are escaping is not the
                            // beginning of a multi-byte character.
                            if (Verifier.isHighSurrogate(ch)) {
                                // this is a the high of a surrogate pair
                                highsurrogate = ch
                            } else {
                                sb.append("&#x" + Integer.toHexString(ch.code) + ";")
                            }
                        } else {
                            sb.append(ch)
                        }
                }
            }
            if (highsurrogate.code > 0) {
                throw IllegalDataException(
                    "Surrogate pair 0x" +
                        Integer.toHexString(highsurrogate.code) + "truncated"
                )
            }

            return sb.toString()
        }

        private fun chooseStrategy(encoding: String): EscapeStrategy {
            if ("UTF-8".equals(encoding, ignoreCase = true) ||
                "UTF-16".equals(encoding, ignoreCase = true)
            ) {
                return UTFEscapeStrategy
            }

            // Note issue #149: https://github.com/hunterhacker/jdom/issues/149
            // require locale for case conversion to avoid potential security issue.
            // (kdom: Kotlin's uppercase() is locale-invariant.)
            val upper = encoding.uppercase()
            if (upper.startsWith("ISO-8859-") ||
                "Latin1".equals(encoding, ignoreCase = true)
            ) {
                return Bits8EscapeStrategy
            }

            if ("US-ASCII".equals(encoding, ignoreCase = true) ||
                "ASCII".equals(encoding, ignoreCase = true)
            ) {
                return Bits7EscapeStrategy
            }

            // kdom: jdom2 asked java.nio.charset here (Charset.forName(encoding).newEncoder().canEncode(ch), and
            // only high surrogates for an unknown charset name). Common code has no charset tables, so the
            // remaining names are mapped explicitly (see translation-notes.md, Behaviour differences):
            // other UTF names escape nothing beyond markup, ISO8859_1 is the 8-bit Latin-1, everything else
            // escapes every non-ASCII character.
            if (upper == "UTF8" || upper.startsWith("UTF-16") || upper.startsWith("UTF-32")) {
                return UTFEscapeStrategy
            }
            if (upper == "ISO8859_1") {
                return Bits8EscapeStrategy
            }
            return Bits7EscapeStrategy
        }
    }
}

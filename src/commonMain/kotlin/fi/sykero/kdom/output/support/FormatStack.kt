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

 */
package fi.sykero.kdom.output.support

import fi.sykero.kdom.internal.ArrayCopy
import fi.sykero.kdom.output.EscapeStrategy
import fi.sykero.kdom.output.Format
import fi.sykero.kdom.output.Format.TextMode

/**
 * FormatStack implements a mechanism where the formatting details can be
 * changed mid-tree, but then get reverted when that tree segment is
 * complete.
 *
 * This class is intended as a working-class for in the various outputter
 * implementations. It is only public so that people extending the
 * Abstract*Processor classes can take advantage of its functionality.
 *
 * The value this class adds is:
 *  * Fast -
 *
 * @since JDOM2
 * @author Rolf Lear
 */
class FormatStack(format: Format) {

    private var capacity = 16 // can grow if more than 16 levels in XML
    private var depth = 0 // current level in XML

    /*
     * ====================================================================
     * The following values cannot be changed mid-way through the output
     * ====================================================================
     */

    /** The base/initial Text mode: the TextMode that was originally set for this stack before any modifications. */
    val defaultMode: TextMode

    /** The original [Format.indent], may be null. The default indent is no spaces (as original document) */
    val indent: String?

    /** The original [Format.encoding]. */
    val encoding: String

    /** The original [Format.lineSeparator]. New line separator */
    val lineSeparator: String?

    /**
     * The original [Format.omitDeclaration]: whether or not to output the XML declaration - default is
     * `false`
     */
    val isOmitDeclaration: Boolean

    /**
     * The original [Format.omitEncoding]: whether or not to output the encoding in the XML declaration -
     * default is `false`
     */
    val isOmitEncoding: Boolean

    /**
     * The original [Format.expandEmptyElements]: whether or not to expand empty elements to
     * &lt;tagName&gt;&lt;/tagName&gt; - default is `false`
     */
    val isExpandEmptyElements: Boolean

    /**
     * Indicate whether only those Attributes specified in the XML
     * should be output: true if only the specified Attributes should be output,
     * false if those Attributes defaulted from the DTD or XML schema
     * should be output too.
     */
    val isSpecifiedAttributesOnly: Boolean

    /** The original [Format.escapeStrategy]: entity escape logic */
    val escapeStrategy: EscapeStrategy

    /*
     * ====================================================================
     * The following values can be changed mid-way through the output, hence
     * they are arrays.
     * ====================================================================
     */

    /** The 'current' accumulated indent */
    private var levelIndents: Array<String?> = arrayOfNulls(capacity)

    /** The 'current' End-Of-Line */
    private var levelEOLs: Array<String?> = arrayOfNulls(capacity)

    /** The padding to put between content items */
    private var levelEOLIndents: Array<String?> = arrayOfNulls(capacity)

    /** The padding to put after the last item (typically one less indent) */
    private var termEOLIndents: Array<String?> = arrayOfNulls(capacity)

    /**
     * Whether TrAX output escaping disabling/enabling PIs are ignored or
     * processed - default is `false`
     */
    private var ignoreTrAXEscapingPIsStack = BooleanArray(capacity)

    /** text handling mode */
    private var modes: Array<TextMode?> = arrayOfNulls(capacity)

    /** escape Output logic - can be changed by */
    private var escapeOutputs = BooleanArray(capacity)

    init {
        indent = format.indent
        lineSeparator = format.lineSeparator

        encoding = format.encoding
        isOmitDeclaration = format.omitDeclaration
        isOmitEncoding = format.omitEncoding
        isExpandEmptyElements = format.expandEmptyElements
        escapeStrategy = format.escapeStrategy
        defaultMode = format.textMode
        isSpecifiedAttributesOnly = format.isSpecifiedAttributesOnly

        modes[depth] = format.textMode
        if (modes[depth] == TextMode.PRESERVE) {
            // undo any special indenting and end-of-line management:
            levelIndents[depth] = null
            levelEOLs[depth] = null
            levelEOLIndents[depth] = null
            termEOLIndents[depth] = null
        } else {
            levelIndents[depth] = if (format.indent == null) null else ""
            levelEOLs[depth] = format.lineSeparator
            levelEOLIndents[depth] = if (levelIndents[depth] == null) null else levelEOLs[depth]
            termEOLIndents[depth] = levelEOLIndents[depth]
        }
        ignoreTrAXEscapingPIsStack[depth] = format.ignoreTrAXEscapingPIs
        escapeOutputs[depth] = true
    }

    /**
     * If the indent strategy changes part way through a stack, we need to
     * clear the previously calculated reusable 'lower' levels of the stack.
     */
    private fun resetReusableIndents() {
        var d = depth + 1
        while (d < levelIndents.size && levelIndents[d] != null) {
            // all subsequent forays in to lower levels will need to be redone
            levelIndents[d] = null
            d++
        }
    }

    /**
     * The current depth's [Format.ignoreTrAXEscapingPIs]
     * (settable for the current depth).
     */
    var isIgnoreTrAXEscapingPIs: Boolean
        get() = ignoreTrAXEscapingPIsStack[depth]
        set(ignoreTrAXEscapingPIs) {
            this.ignoreTrAXEscapingPIsStack[depth] = ignoreTrAXEscapingPIs
        }

    /**
     * The escapeOutput flag can be set or unset. When set, Element text and
     * Attribute values are 'escaped' so that the output is valid XML. When
     * unset, the Element text and Attribute values are not escaped.
     * (The current depth's escapeOutput flag.)
     */
    var escapeOutput: Boolean
        get() = escapeOutputs[depth]
        set(escape) {
            escapeOutputs[depth] = escape
        }

    /**
     * The current depth's accumulated/maintained indent, may be null.
     * Setting it overrides the current depth's accumulated line indent.
     */
    var levelIndent: String?
        get() = levelIndents[depth]
        set(indent) {
            this.levelIndents[depth] = indent
            levelEOLIndents[depth] = if (indent == null || levelEOLs[depth] == null) null
            else (levelEOLs[depth] + indent)
            resetReusableIndents()
        }

    /**
     * Get the end-of-line indenting sequence for before the first item in an
     * Element, as well as between subsequent items (but not after the last item):
     * the String EOL sequence followed by an indent. Null if it should
     * be ignored
     */
    val padBetween: String?
        get() = levelEOLIndents[depth]

    /**
     * Get the end-of-line indenting sequence for after the last item in an
     * Element: the String EOL sequence followed by an indent. Null if it should
     * be ignored
     */
    val padLast: String?
        get() = termEOLIndents[depth]

    /**
     * The current depth's End-Of-Line sequence, may be null.
     * Setting it changes the current depth's End-Of-Line sequence.
     */
    var levelEOL: String?
        get() = levelEOLs[depth]
        set(newline) {
            this.levelEOLs[depth] = newline
            resetReusableIndents()
        }

    /**
     * The current depth's [Format.textMode].
     * Setting it changes the current level's TextMode.
     */
    var textMode: TextMode
        get() = modes[depth]!!
        set(mode) {
            if (this.modes[depth] == mode) {
                return
            }
            this.modes[depth] = mode
            when (mode) {
                TextMode.PRESERVE -> {
                    levelEOLs[depth] = null
                    levelIndents[depth] = null
                    levelEOLIndents[depth] = null
                    termEOLIndents[depth] = null
                }
                else -> {
                    levelEOLs[depth] = lineSeparator
                    if (indent == null || lineSeparator == null) {
                        levelEOLIndents[depth] = null
                        termEOLIndents[depth] = null
                    } else {
                        if (depth > 0) {
                            val sb = StringBuilder(indent.length * depth)
                            for (i in 1 until depth) {
                                sb.append(indent)
                            }
                            // the start point was '1', so we are one indent
                            // short, which is just right for the term....
                            termEOLIndents[depth] = lineSeparator + sb.toString()
                            // but we increase it once for the actual indent.
                            sb.append(indent)
                            levelIndents[depth] = sb.toString()
                        } else {
                            termEOLIndents[depth] = lineSeparator
                            levelIndents[depth] = ""
                        }
                        levelEOLIndents[depth] = lineSeparator + levelIndents[depth]
                    }
                }
            }
            resetReusableIndents()
        }

    /**
     * Create a new depth level on the stack. The previous level's details
     * are copied to this level, and the accumulated indent (if any) is
     * indented further.
     */
    fun push() {
        val prev = depth++
        if (depth >= capacity) {
            capacity *= 2
            levelIndents = ArrayCopy.copyOf(levelIndents, capacity)
            levelEOLs = ArrayCopy.copyOf(levelEOLs, capacity)
            levelEOLIndents = ArrayCopy.copyOf(levelEOLIndents, capacity)
            termEOLIndents = ArrayCopy.copyOf(termEOLIndents, capacity)
            ignoreTrAXEscapingPIsStack = ArrayCopy.copyOf(ignoreTrAXEscapingPIsStack, capacity)
            modes = ArrayCopy.copyOf(modes, capacity)
            escapeOutputs = ArrayCopy.copyOf(escapeOutputs, capacity)
        }

        ignoreTrAXEscapingPIsStack[depth] = ignoreTrAXEscapingPIsStack[prev]
        modes[depth] = modes[prev]
        escapeOutputs[depth] = escapeOutputs[prev]

        if (levelIndents[prev] == null || levelEOLs[prev] == null) {
            levelIndents[depth] = null
            levelEOLs[depth] = null
            levelEOLIndents[depth] = null
            termEOLIndents[depth] = null
        } else if (levelIndents[depth] == null) {
            // we need to build our level details ....
            // cannot reuse previous ones.
            levelEOLs[depth] = levelEOLs[prev]
            termEOLIndents[depth] = levelEOLs[depth] + levelIndents[prev]
            levelIndents[depth] = levelIndents[prev] + indent
            levelEOLIndents[depth] = levelEOLs[depth] + levelIndents[depth]
        }
    }

    /**
     * Move back a level on the stack.
     */
    fun pop() {
        // no need to clear previously used members in the stack.
        // the stack is short-lived, and does not create new instances for
        // the depth levels, in other words, it does not affect GC and does
        // not save memory to clear the stack.
        depth--
    }
}

/*-- 

 Copyright (C) 2012 Jason Hunter & Brett McLaughlin.
 All rights reserved.

 Redistribution and use in mtsource and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of mtsource code must retain the above copyright
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

import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Content
import fi.sykero.kdom.internal.ArrayCopy
import fi.sykero.kdom.output.EscapeStrategy
import fi.sykero.kdom.output.Format

/**
 * This Walker implementation walks a list of Content in a Formatted form of
 * some sort.
 *
 * The JDOM content can be loosely categorised in to 'Text-like' content
 * (consisting of Text, CDATA, and EntityRef), and everything else. This
 * distinction is significant for for this class and its sub-classes.
 *
 * There will be text manipulation, and some (but not necessarily
 * all) Text-like content will be returned as text() instead of next().
 *
 * The trick in this class is that it deals with the regular content, and
 * delegates the Text-like content to the sub-classes.
 *
 * Subclasses are tasked with analysing chunks of Text-like content in the
 * [analyzeMultiText] method. The subclasses are
 * responsible for adding the relevant text content to the suppliedMultiText
 * instance in such a way as to result in the correct format.
 *
 * The Subclass needs to concern itself with only the text portion because this
 * abstract class will ensure the Text-like content is appropriately indented.
 *
 * Create a Walker that preserves all content in its raw state.
 * @param xx the content to walk.
 * @param fstack the current FormatStack
 * @param doescape Whether Text values should be escaped.
 *
 * @author Rolf Lear
 */
abstract class AbstractFormattedWalker(
    xx: List<Content>,
    private val fstack: FormatStack,
    doescape: Boolean
) : Walker {

    /**
     * Indicate how text content should be added
     * @author Rolf Lear
     */
    protected enum class Trim {
        /** Left Trim  */
        LEFT,

        /** Right Trim  */
        RIGHT,

        /** Both Trim  */
        BOTH,

        /** Trim Both and replace all internal whitespace with a single space */
        COMPACT,

        /** No Trimming at all  */
        NONE
    }

    /**
     * Collect together the items that constitute formatted Text-like content.
     *
     * (The constructor is internal: only this abstract class can create instances.)
     *
     * @author Rolf Lear
     */
    protected inner class MultiText internal constructor() {

        /**
         * Ensure we have space for at least one more text-like item.
         */
        private fun ensurespace() {
            if (mtsize >= mtdata.size) {
                mtdata = ArrayCopy.copyOf(mtdata, mtsize + 1 + (mtsize / 2))
                mttext = ArrayCopy.copyOf(mttext, mtdata.size)
            }
        }

        /**
         * Handle the case where we have been accumulating true text content,
         * and the next item is not more text.
         */
        private fun closeText() {
            if (mtbuffer.length == 0) {
                // empty text does not need adding at all.
                return
            }
            ensurespace()
            mtdata[mtsize] = null
            mttext[mtsize++] = mtbuffer.toString()
            mtbuffer.clear()
        }

        /**
         * Append some text to the text-like sequence that will be treated as
         * plain XML text (PCDATA). If the last content added to this text-like
         * sequence then this new text will be appended directly to the previous
         * text.
         *
         * @param trim How to prepare the Text content
         * @param text The actual Text content.
         */
        fun appendText(trim: Trim, text: String) {
            val tlen = text.length
            if (tlen == 0) {
                return
            }
            var toadd: String? = null
            when (trim) {
                Trim.NONE -> toadd = text
                Trim.BOTH -> toadd = Format.trimBoth(text)
                Trim.LEFT -> toadd = Format.trimLeft(text)
                Trim.RIGHT -> toadd = Format.trimRight(text)
                Trim.COMPACT -> toadd = Format.compact(text)
            }
            if (toadd != null) {
                toadd = escapeText(toadd)
                mtbuffer.append(toadd)
                mtgottext = true
            }
        }

        private fun escapeText(text: String): String {
            if (escape == null || !fstack.escapeOutput) {
                return text
            }
            return Format.escapeText(escape, endofline, text)
        }

        private fun escapeCDATA(text: String): String {
            if (escape == null) {
                return text
            }
            return text
        }

        /**
         * Append some text to the text-like sequence that will be treated as
         * CDATA.
         * @param trim How to prepare the CDATA content
         * @param text The actual CDATA content.
         */
        fun appendCDATA(trim: Trim, text: String) {
            // this resets the mtbuffer too.
            closeText()
            var toadd: String = when (trim) {
                Trim.NONE -> text
                Trim.BOTH -> Format.trimBoth(text)
                Trim.LEFT -> Format.trimLeft(text)
                Trim.RIGHT -> Format.trimRight(text)
                Trim.COMPACT -> Format.compact(text)
            }

            toadd = escapeCDATA(toadd)
            ensurespace()
            // mark this as being CDATA text
            mtdata[mtsize] = CDATATOKEN
            mttext[mtsize++] = toadd

            mtgottext = true
        }

        /**
         * Simple method that ensures the text is processed, regardless of
         * content, and is never escaped.
         * (Private in jdom2; internal here because the enclosing class calls it.)
         * @param text
         */
        internal fun forceAppend(text: String) {
            mtgottext = true
            mtbuffer.append(text)
        }

        /**
         * Add some JDOM Content (typically an EntityRef) that will be treated
         * as part of the Text-like sequence.
         * @param c the content to add.
         */
        fun appendRaw(c: Content) {
            closeText()
            ensurespace()
            mttext[mtsize] = null
            mtdata[mtsize++] = c
            mtbuffer.clear()
        }

        /**
         * Indicate that there is no further content to be added to the
         * text-like sequence.
         */
        fun done() {
            if (mtpostpad && newlineindent != null) {
                // this will be ignored if there was not some content.
                mtbuffer.append(newlineindent)
            }
            if (mtgottext) {
                closeText()
            }
            mtbuffer.clear()
        }
    }

    private var pending: Content? = null
    private val content: Iterator<Content> = if (xx.isEmpty()) EMPTYIT else xx.iterator()
    private val alltext: Boolean
    private val allwhite: Boolean
    private val newlineindent: String? = fstack.padBetween
    private val endofline: String? = fstack.levelEOL
    private val escape: EscapeStrategy? = if (doescape) fstack.escapeStrategy else null
    private var hasnext = true

    // MultiText handling changed in 2.0.5
    // MultiText is something quite complicated, but it goes something like this:
    // XML Content is either text-like, or its not. If we encounter text-like content
    // then we find out how many text-like contents are in a row, and we add them to a
    // multi-text. We then either get to the end of the content, or a non-text content.
    // If we complete the multitext, we then move on to the non-text item, and we set multitext
    // to null. Both multitect and pendingmt are thus null.
    // If the content following the non-text is then text-like, we populate pendingmt.
    // bottom line is that multitext and pendingmt can never both be set.
    // we use one set of variables to back up both of them. This is fast, and safe in a single
    // threaded environment (which the Walkers are guaranteed to be in).
    // all MultiText-specific variables have the names mt*
    private var multitext: MultiText? = null
    private var pendingmt: MultiText? = null
    private val holdingmt = MultiText()

    private val mtbuffer = StringBuilder()

    // if there should be indenting after this text.
    private var mtpostpad = false

    // indicate whether there is something actually added.
    private var mtgottext = false

    // the number of mixed content values.
    private var mtsize = 0
    private var mtsourcesize = 0
    private var mtsource: Array<Content?> = arrayOfNulls(8)

    // the location of the processed content.
    private var mtdata: Array<Content?> = arrayOfNulls(8)

    // whether the mixed content should be returned as raw JDOM objects
    private var mttext: Array<String?> = arrayOfNulls(8)

    // the current cursor in the mixed content.
    private var mtpos = -1

    // we cheat here by using Boolean as a three-state option...
    // we expect it to be null often.
    private var mtwasescape: Boolean? = null

    init {
        if (!content.hasNext()) {
            alltext = true
            allwhite = true
        } else {
            var atext = false
            var awhite = false
            pending = content.next()
            if (isTextLike(pending!!)) {
                // the first item in the list is Text-like, and we pre-check
                // to see whether all content is text.... and whether it amounts
                // to something.
                pendingmt = buildMultiText(true)
                analyzeMultiText(pendingmt!!, 0, mtsourcesize)
                pendingmt!!.done()

                if (pending == null) {
                    atext = true
                    awhite = mtsize == 0
                }
                if (mtsize == 0) {
                    // first content in list is ignorable.
                    pendingmt = null
                }
            }
            alltext = atext
            allwhite = awhite
        }
        hasnext = pendingmt != null || pending != null
    }

    final override fun next(): Content? {

        if (!hasnext) {
            throw NoSuchElementException("Cannot walk off end of Content")
        }

        if (multitext != null && mtpos + 1 >= mtsize) {
            // finished this multitext. need to move on.
            multitext = null
            resetMultiText()
        }
        if (pendingmt != null) {
            // we have a multi-text pending from the last block
            // this will only be the case when the previous value was non-text.
            val wasescape = mtwasescape
            if (wasescape != null &&
                fstack.escapeOutput != wasescape
            ) {
                // we calculated pending with one escape strategy, but it changed...
                // we need to recalculate it....

                mtsize = 0
                mtwasescape = fstack.escapeOutput
                analyzeMultiText(pendingmt!!, 0, mtsourcesize)
                pendingmt!!.done()
            }
            multitext = pendingmt
            pendingmt = null
        }

        if (multitext != null) {

            // OK, we have text-like content to push back.
            // and it still has values in it.
            // advance the cursor
            mtpos++

            val ret = if (mttext[mtpos] == null) mtdata[mtpos] else null

            // we can calculate the hasnext
            hasnext = mtpos + 1 < mtsize ||
                pending != null

            // return null to indicate text content.
            return ret
        }

        // non-text, increment and return content.
        val ret = pending
        pending = if (content.hasNext()) content.next() else null

        // OK, we are returning some content.
        // we need to determine the state of the next loop.
        // cursor at this point has been advanced!
        if (pending == null) {
            hasnext = false
        } else {
            // there is some more content.
            // we need to inspect it to determine whether it is good
            if (isTextLike(pending!!)) {
                // calculate what this next text-like content looks like.
                pendingmt = buildMultiText(false)
                analyzeMultiText(pendingmt!!, 0, mtsourcesize)
                pendingmt!!.done()

                if (mtsize > 0) {
                    hasnext = true
                } else {
                    // all white text... perhaps we need indenting anyway.
                    // buildMultiText has moved on the pending value....
                    if (pending != null && newlineindent != null) {
                        // yes, we need indenting.
                        // redefine the pending.
                        resetMultiText()
                        pendingmt = holdingmt
                        pendingmt!!.forceAppend(newlineindent)
                        pendingmt!!.done()
                        hasnext = true
                    } else {
                        pendingmt = null
                        hasnext = pending != null
                    }
                }
            } else {
                // it is non-text content... we have more content.
                // but, we just returned non-text content. We may need to indent
                if (newlineindent != null) {
                    resetMultiText()
                    pendingmt = holdingmt
                    pendingmt!!.forceAppend(newlineindent)
                    pendingmt!!.done()
                }
                hasnext = true
            }
        }
        return ret
    }

    private fun resetMultiText() {
        mtsourcesize = 0
        mtpos = -1
        mtsize = 0
        mtgottext = false
        mtpostpad = false
        mtwasescape = null
        mtbuffer.clear()
    }

    /**
     * Add the content at the specified indices to the provided MultiText.
     * @param mtext the MultiText to append to.
     * @param offset The first Text-like content to add to the MultiText
     * @param len The number of Text-like content items to add.
     */
    protected abstract fun analyzeMultiText(mtext: MultiText, offset: Int, len: Int)

    /**
     * Get the content at a position in the input content. Useful for subclasses
     * in their [analyzeMultiText] calls.
     * @param index the index to get the content at.
     * @return the content at the index.
     */
    protected fun get(index: Int): Content {
        return mtsource[index]!!
    }

    final override val isAllText: Boolean
        get() = alltext

    final override fun hasNext(): Boolean {
        return hasnext
    }

    /**
     * This method was changed in 2.0.5
     * It now is only called when building the content of the variable pendingmt
     * This is important, because only pendingmt can be referenced when analyzing
     * the MultiText content.
     * @param first
     * @return The updated MultiText containing the correct sequence of Text-like content
     */
    private fun buildMultiText(first: Boolean): MultiText {
        // set up a sequence where the next bunch of stuff is text.
        if (!first && newlineindent != null) {
            mtbuffer.append(newlineindent)
        }
        mtsourcesize = 0
        do {
            if (mtsourcesize >= mtsource.size) {
                mtsource = ArrayCopy.copyOf(mtsource, mtsource.size * 2)
            }
            mtsource[mtsourcesize++] = pending
            pending = if (content.hasNext()) content.next() else null
        } while (pending != null && isTextLike(pending!!))

        mtpostpad = pending != null
        mtwasescape = fstack.escapeOutput
        return holdingmt
    }

    final override fun text(): String? {
        if (multitext == null || mtpos >= mtsize) {
            return null
        }
        return mttext[mtpos]
    }

    final override val isCDATA: Boolean
        get() {
            if (multitext == null || mtpos >= mtsize) {
                return false
            }
            if (mttext[mtpos] == null) {
                return false
            }

            return mtdata[mtpos] === CDATATOKEN
        }

    final override val isAllWhitespace: Boolean
        get() = allwhite

    private fun isTextLike(c: Content): Boolean {
        when (c.cType) {
            Content.CType.Text,
            Content.CType.CDATA,
            Content.CType.EntityRef -> return true
            else -> {
                // nothing.
            }
        }
        return false
    }

    private companion object {
        /*
         * We use Text instances to return formatted text to the caller.
         * We do not need to validate the Text content... it is 'safe' to
         * not use the default Text class.
         */
        private val CDATATOKEN = CDATA("")

        private val EMPTYIT: Iterator<Content> = object : MutableIterator<Content> {
            override fun hasNext(): Boolean {
                return false
            }

            override fun next(): Content {
                throw NoSuchElementException("Cannot call next() on an empty iterator.")
            }

            override fun remove() {
                throw UnsupportedOperationException("Cannot remove from an empty iterator.")
            }
        }
    }
}

package fi.sykero.kdom.input

import fi.sykero.kdom.sax.SAXParseException

/**
 * A same-length copy of the source with a few characters replaced, so that xmlutil's reader treats them as SAX
 * (XML 1.0) does; [name] and [content] undo the replacement in what the reader reports. Offsets are preserved, so
 * the reader's line/column positions stay those of the original text.
 *
 * - U+0085 and U+2028 (anywhere): xmlutil applies XML 1.1 line-end handling to them (turns them into `\n`, or a space in
 *   attribute values); SAX 1.0 keeps them. They become private-use characters that occur neither in the source nor as
 *   a character reference, and [content] turns them back.
 * - A carriage return not followed by a line feed becomes a line feed (xmlutil folds `\r\r` into one line end; SAX
 *   reports two). Not restored: XML line-end normalization turns it into a line feed anyway.
 * - Namespace-unaware mode (requirement P4): xmlutil's reader always processes namespaces and rejects `<p:a/>` with an
 *   undeclared prefix. Each `:` inside a name becomes a name character absent from the source, and the final `s` of
 *   an attribute named exactly `xmlns` another one, so the reader sees plain names and no namespace declarations;
 *   [name] restores `:` and `s`.
 *
 * Both replacement sets are chosen per document from characters absent from it, so restoring is exact.
 */
internal class XmlSourceMask private constructor(
    val masked: String,
    private val colonMask: Char,
    private val xmlnsMask: Char,
    private val nelMask: Char,
    private val lsepMask: Char,
) {
    private val hasNameMasks = colonMask != NONE || xmlnsMask != NONE
    private val hasContentMasks = nelMask != NONE || lsepMask != NONE

    /** Restores a name (element or attribute) reported by the reader. */
    fun name(s: String): String {
        if (!hasNameMasks) return s
        var r = s
        if (colonMask != NONE) r = r.replace(colonMask, ':')
        if (xmlnsMask != NONE) r = r.replace(xmlnsMask, 's')
        return r
    }

    /** Restores character data, attribute values, comments and processing instruction text reported by the reader. */
    fun content(s: String): String {
        if (!hasContentMasks) return s
        var r = s
        if (nelMask != NONE) r = r.replace(nelMask, '\u0085')
        if (lsepMask != NONE) r = r.replace(lsepMask, '\u2028')
        return r
    }

    companion object {
        private const val NONE = '\u0000'

        /** Builds the masked copy for [source] after [scan], or returns `null` when nothing has to be masked. */
        fun create(source: String, scan: SourceScanner): XmlSourceMask? {
            val hasNel = scan.lineEndChars.any { source[it] == '\u0085' }
            val hasLsep = scan.lineEndChars.any { source[it] == '\u2028' }
            if (scan.nameColons.isEmpty() && scan.xmlnsNames.isEmpty() && !hasNel && !hasLsep &&
                scan.loneCarriageReturns.isEmpty()) return null
            val present = BooleanArray(65536)
            for (c in source) present[c.code] = true
            for (cp in scan.privateUseRefs) present[cp] = true
            // Name masks: name characters (start and inner) for xmlutil, from U+2C00..U+2FEF.
            val nameCandidates = ('\u2C00'..'\u2FEF').asSequence().filter { !present[it.code] }.iterator()
            // Content masks: plain private-use characters (legal XML characters, no special meaning for the reader).
            val contentCandidates = ('\uE000'..'\uF8FF').asSequence().filter { !present[it.code] }.iterator()
            val colon = if (scan.nameColons.isNotEmpty()) take(nameCandidates) else NONE
            val xmlns = if (scan.xmlnsNames.isNotEmpty()) take(nameCandidates) else NONE
            val nel = if (hasNel) take(contentCandidates) else NONE
            val lsep = if (hasLsep) take(contentCandidates) else NONE
            val chars = source.toCharArray()
            for (k in scan.nameColons) chars[k] = colon
            for (k in scan.xmlnsNames) chars[k] = xmlns
            for (k in scan.lineEndChars) chars[k] = if (chars[k] == '\u0085') nel else lsep
            // A lone CR is a line end (XML 1.0 2.11): writing the LF it normalizes to keeps lines and offsets, and the
            // reader then normalizes it as SAX does (LF in content, a space in attribute values). Needs no restore.
            for (k in scan.loneCarriageReturns) chars[k] = '\n'
            return XmlSourceMask(chars.concatToString(), colon, xmlns, nel, lsep)
        }

        private fun take(candidates: Iterator<Char>): Char {
            // Only a document that uses every candidate character could exhaust a range.
            if (!candidates.hasNext()) throw SAXParseException("kdom cannot parse this document: no free mask character", -1, -1)
            return candidates.next()
        }
    }
}

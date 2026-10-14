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
 * - Permissive DOCTYPE mode (R11): the DOCTYPE (already read by [SourceScanner]) becomes a processing instruction
 *   `<?T?>` whose target T is [docTypeTarget] (a name character absent from the source) followed by white space up to
 *   the DOCTYPE's length (line ends kept), so xmlutil
 *   neither sees nor expands it and the parser reports the DOCTYPE when that PI arrives. Each non-predefined entity
 *   reference `&name;` becomes a mark character followed by its index into [SourceScanner.entityUses] written in
 *   base-256 "digit" characters (all absent from the source), so the reader passes it through as text; [content] and
 *   [textSegments] put the expansion (or the skipped entity) back.
 *
 * All replacement characters are chosen per document from characters absent from it, so restoring is exact.
 */
internal class XmlSourceMask private constructor(
    val masked: String,
    private val colonMask: Char,
    private val xmlnsMask: Char,
    private val nelMask: Char,
    private val lsepMask: Char,
    /** PI target that stands for the DOCTYPE, or null. */
    val docTypeTarget: String?,
    private val entityMark: Char,
    private val digitBase: Char,
    private val uses: List<EntityUse>,
) {
    private val hasNameMasks = colonMask != NONE || xmlnsMask != NONE
    private val hasContentMasks = nelMask != NONE || lsepMask != NONE || entityMark != NONE

    /** Restores a name (element or attribute) reported by the reader. */
    fun name(s: String): String {
        if (!hasNameMasks) return s
        var r = s
        if (colonMask != NONE) r = r.replace(colonMask, ':')
        if (xmlnsMask != NONE) r = r.replace(xmlnsMask, 's')
        return r
    }

    /**
     * Restores character data, attribute values, namespace URIs, comments and processing instruction text reported by the
     * reader; an entity placeholder becomes its expansion (a skipped entity, possible only in text, see [textSegments],
     * becomes nothing).
     */
    fun content(s: String): String {
        if (!hasContentMasks) return s
        val sb = StringBuilder(s.length)
        textSegments(s, { sb.append(it) }, {})
        return sb.toString()
    }

    /** Splits reader text into restored text runs ([onText]) and skipped entities ([onSkip]) in order. */
    fun textSegments(s: String, onText: (String) -> Unit, onSkip: (String) -> Unit) {
        if (!hasContentMasks) { if (s.isNotEmpty()) onText(s); return }
        val sb = StringBuilder(s.length)
        var k = 0
        while (k < s.length) {
            val c = s[k]
            when {
                entityMark != NONE && c == entityMark -> {
                    var index = 0
                    k++
                    while (k < s.length && s[k] - digitBase in 0..255) { index = index * 256 + (s[k] - digitBase); k++ }
                    when (val use = uses[index]) {
                        is EntityUse.Expand -> sb.append(use.text)
                        is EntityUse.Skip -> {
                            if (sb.isNotEmpty()) { onText(sb.toString()); sb.clear() }
                            onSkip(use.name)
                        }
                    }
                    continue
                }
                nelMask != NONE && c == nelMask -> sb.append('\u0085')
                lsepMask != NONE && c == lsepMask -> sb.append('\u2028')
                else -> sb.append(c)
            }
            k++
        }
        if (sb.isNotEmpty()) onText(sb.toString())
    }

    companion object {
        private const val NONE = '\u0000'

        /** Builds the masked copy for [source] after [scan], or returns `null` when nothing has to be masked. */
        fun create(source: String, scan: SourceScanner): XmlSourceMask? {
            val hasNel = scan.lineEndChars.any { source[it] == '\u0085' }
            val hasLsep = scan.lineEndChars.any { source[it] == '\u2028' }
            val docType = scan.docType
            if (scan.nameColons.isEmpty() && scan.xmlnsNames.isEmpty() && !hasNel && !hasLsep &&
                scan.loneCarriageReturns.isEmpty() && docType == null) return null
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
            val docMark = if (docType != null) take(nameCandidates) else NONE
            val entityMark = if (scan.entityRefs.isNotEmpty()) take(contentCandidates) else NONE
            // 256 consecutive free private-use characters serve as base-256 digits.
            var digitBase = NONE
            if (entityMark != NONE) {
                var run = 0
                var c = 0xE000
                while (c <= 0xF8FF && run < 256) {
                    if (present[c] || c == nel.code || c == lsep.code || c == entityMark.code) run = 0 else run++
                    c++
                }
                if (run < 256) throw SAXParseException("kdom cannot parse this document: no free mask characters", -1, -1)
                digitBase = (c - 256).toChar()
            }
            val chars = source.toCharArray()
            for (k in scan.nameColons) chars[k] = colon
            for (k in scan.xmlnsNames) chars[k] = xmlns
            for (k in scan.lineEndChars) chars[k] = if (chars[k] == '\u0085') nel else lsep
            // A lone CR is a line end (XML 1.0 2.11): writing the LF it normalizes to keeps lines and offsets, and the
            // reader then normalizes it as SAX does (LF in content, a space in attribute values). Needs no restore.
            for (k in scan.loneCarriageReturns) chars[k] = '\n'
            for (ref in scan.entityRefs) {
                var index = ref[2]
                chars[ref[0]] = entityMark
                for (k in ref[1] - 1 downTo ref[0] + 1) { chars[k] = digitBase + (index and 0xFF); index = index shr 8 }
                if (index != 0) throw SAXParseException("kdom cannot parse this document: too many entity references", -1, -1)
            }
            if (docType != null) {
                // "<?" + target + "?>" over "<!DOC", then prolog white space (line ends kept) up to the DOCTYPE's end.
                for (k in docType.start until docType.end) if (chars[k] != '\n' && chars[k] != '\r') chars[k] = ' '
                chars[docType.start] = '<'
                chars[docType.start + 1] = '?'
                chars[docType.start + 2] = docMark
                chars[docType.start + 3] = '?'
                chars[docType.start + 4] = '>'
            }
            return XmlSourceMask(
                chars.concatToString(), colon, xmlns, nel, lsep,
                if (docType != null) docMark.toString() else null, entityMark, digitBase, scan.entityUses,
            )
        }

        private fun take(candidates: Iterator<Char>): Char {
            // Only a document that uses every candidate character could exhaust a range.
            if (!candidates.hasNext()) throw SAXParseException("kdom cannot parse this document: no free mask character", -1, -1)
            return candidates.next()
        }
    }
}

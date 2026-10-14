package fi.sykero.kdom.input

import fi.sykero.kdom.Verifier
import fi.sykero.kdom.sax.SAXParseException

/** XML 1.0 white space (`S` production). */
internal fun isXmlSpace(c: Char): Boolean = c == ' ' || c == '\t' || c == '\n' || c == '\r'

/** XML 1.0 `Char` production for a code point. */
internal fun isXmlCodePoint(cp: Int): Boolean =
    cp == 0x9 || cp == 0xA || cp == 0xD || cp in 0x20..0xD7FF || cp in 0xE000..0xFFFD || cp in 0x10000..0x10FFFF

/** 1-based line and column of [offset] in [s]; `\r\n`, `\r` and `\n` each end a line (XML 1.0 line ends). */
internal fun lineColumnOf(s: CharSequence, offset: Int): IntArray {
    var line = 1
    var col = 1
    var i = 0
    val end = minOf(offset, s.length)
    while (i < end) {
        when (s[i]) {
            '\n' -> { line++; col = 1 }
            '\r' -> {
                line++; col = 1
                if (i + 1 < end && s[i + 1] == '\n') i++
            }
            else -> col++
        }
        i++
    }
    return intArrayOf(line, col)
}

/**
 * Raw-source pre-scan of a document before xmlutil reads it. It enforces the well-formedness rules that xmlutil
 * 1.0.2.1's reader does not (docs/xmlutil-probes.md):
 *
 * - P5: every character of the document is an XML 1.0 `Char` (surrogates paired);
 * - P6: no literal `<` in a quoted attribute value (ruling R7);
 * - P8: every character reference names a legal `Char`, and is well-formed (`&#;`, `&#x;` are rejected);
 * - P7: no `<!DOCTYPE` (plan rule), and no other `<!` markup declaration;
 * - P2 (raw part): no attribute qualified name twice in a start tag (this also catches a repeated `xmlns:p`);
 * - white space between attributes, no `]]>` in character data, no byte order mark character at the start of a
 *   string (SAX: "Content is not allowed in prolog").
 *
 * It also records the positions the parser masks before handing the text to xmlutil (see [XmlSourceMask]):
 * the U+0085 / U+2028 characters (xmlutil applies XML 1.1 line-end handling to them, SAX 1.0 keeps them), and in
 * namespace-unaware mode the colons in element and attribute names and attribute names that are exactly `xmlns`.
 *
 * Comments, CDATA sections and processing instructions are skipped as wholes; their inner syntax is left to xmlutil.
 *
 * With [allowDocType] (permissive mode, R11) a DOCTYPE is read here instead of being rejected: name, external id,
 * internal subset (element, entity and notation declarations, comments, PIs; `ATTLIST` and parameter-entity references
 * are rejected as unsupported). The external subset is never read. The DOCTYPE then decides how a non-predefined entity
 * reference in content or an attribute value is reported ([entityUses], recorded at [entityRefs]); the parser masks the
 * DOCTYPE and these references before xmlutil reads the text.
 */
internal class SourceScanner(
    private val s: String,
    private val namespaceAware: Boolean,
    private val allowDocType: Boolean = false,
) {
    private val n = s.length
    private var i = 0

    /** Offsets of `:` inside element/attribute names (namespace-unaware mode only). */
    val nameColons: MutableList<Int> = ArrayList()

    /** Offsets of the final `s` of attribute names that are exactly `xmlns` (namespace-unaware mode only). */
    val xmlnsNames: MutableList<Int> = ArrayList()

    /** Offsets of carriage returns not followed by a line feed (xmlutil folds `\r\r` into one line end). */
    val loneCarriageReturns: MutableList<Int> = ArrayList()

    /** Offsets of U+0085 and U+2028 characters. */
    val lineEndChars: MutableList<Int> = ArrayList()

    /** Values of character references in the private use range U+E000..U+F8FF (candidates for mask characters). */
    val privateUseRefs: MutableSet<Int> = HashSet()

    /** The DOCTYPE (permissive mode only). */
    var docType: ScannedDocType? = null
        private set

    /** `[start, end, index into entityUses]` of each non-predefined entity reference (permissive mode with a DOCTYPE). */
    val entityRefs: MutableList<IntArray> = ArrayList()
    val entityUses: MutableList<EntityUse> = ArrayList()

    private var seenRoot = false
    private var standaloneYes = false

    fun scan() {
        checkCharacters()
        if (n > 0 && s[0] == '\uFEFF') fail(0, "Content is not allowed in prolog.")
        while (i < n) {
            when (s[i]) {
                '<' -> markup()
                '&' -> i = if (i + 1 < n && s[i + 1] == '#') charRef(i) else if (docType != null) entityRef(i, false) else i + 1
                ']' -> {
                    if (s.startsWith("]]>", i)) {
                        fail(i, "The character sequence \"]]>\" must not appear in content unless used to mark the end of a CDATA section.")
                    }
                    i++
                }
                else -> i++
            }
        }
    }

    private fun checkCharacters() {
        var k = 0
        while (k < n) {
            val c = s[k]
            when {
                c.isHighSurrogate() -> {
                    if (k + 1 < n && s[k + 1].isLowSurrogate()) k++ else invalidChar(k, c)
                }
                c.isLowSurrogate() -> invalidChar(k, c)
                c == '\u0085' || c == '\u2028' -> lineEndChars.add(k)
                c == '\r' -> if (k + 1 >= n || s[k + 1] != '\n') loneCarriageReturns.add(k)
                !isXmlCodePoint(c.code) -> invalidChar(k, c)
            }
            k++
        }
    }

    private fun invalidChar(at: Int, c: Char): Nothing =
        fail(at, "An invalid XML character (Unicode: 0x" + c.code.toString(16) + ") was found in the document.")

    private fun markup() {
        when {
            s.startsWith("<!--", i) -> i = skipPast("-->", i + 4, "comment")
            s.startsWith("<![CDATA[", i) -> i = skipPast("]]>", i + 9, "CDATA section")
            s.startsWith("<!DOCTYPE", i) -> if (allowDocType) docTypeDeclaration() else fail(i, "DOCTYPE is not allowed (kdom does not process DTDs).")
            s.startsWith("<!", i) -> fail(i, "The markup in the document must be well-formed.")
            s.startsWith("<?", i) -> processingInstruction()
            s.startsWith("</", i) -> endTag()
            else -> startTag()
        }
    }

    private fun skipPast(terminator: String, from: Int, what: String): Int {
        val end = s.indexOf(terminator, from)
        if (end < 0) fail(n, "XML document structures must start and end within the same entity (unterminated $what).")
        return end + terminator.length
    }

    /**
     * `<?` Name (S ... )? `?>`: the target must be an XML Name followed by white space or `?>` (SAX: "White space is
     * required between the processing instruction target and data"). The XML declaration also needs white space between
     * its pseudo-attributes.
     */
    private fun processingInstruction() {
        val start = i + 2
        var k = start
        while (k < n && !isXmlSpace(s[k]) && !s.startsWith("?>", k)) k++
        if (k == start) fail(start, "The processing instruction must begin with the name of the target.")
        if (k >= n) fail(n, "XML document structures must start and end within the same entity (unterminated processing instruction).")
        checkName(start, k, "White space is required between the processing instruction target and data.")
        if (i == 0 && k - start == 3 && s.startsWith("xml", start)) xmlDeclaration(k)
        i = skipPast("?>", k, "processing instruction")
    }

    /** After each quoted pseudo-attribute value of the XML declaration comes white space or `?>`. */
    private fun xmlDeclaration(from: Int) {
        val close = s.indexOf("?>", from)
        if (close > 0 && STANDALONE_YES.containsMatchIn(s.substring(from, close))) standaloneYes = true
        var k = from
        var quote = '\u0000'
        while (k < n && !(quote == '\u0000' && s.startsWith("?>", k))) {
            val c = s[k]
            if (quote == '\u0000') {
                if (c == '"' || c == '\'') quote = c
            } else if (c == quote) {
                quote = '\u0000'
                if (k + 1 < n && !isXmlSpace(s[k + 1]) && s[k + 1] != '?') {
                    fail(k + 1, "White space is required between the pseudo attributes of the XML declaration.")
                }
            }
            k++
        }
    }

    /** XML 1.0 (4th edition, as the JDK parser) Name check of `s[from, to)`; kdom's Verifier tables match the JDK's. */
    private fun checkName(from: Int, to: Int, message: String) {
        if (!Verifier.isXMLNameStartCharacter(s[from])) fail(from, message)
        for (k in from + 1 until to) if (!Verifier.isXMLNameCharacter(s[k])) fail(k, message)
    }

    /**
     * Namespace-aware QName rule of the JDK parser: the first `:` after the first character splits prefix and local
     * part; an empty local part or a second `:` in it is rejected, and so is the element prefix `xmlns`. (A leading `:`
     * is part of the local name, as in the JDK parser.)
     */
    private fun checkQName(from: Int, to: Int, element: Boolean) {
        if (!namespaceAware) return
        var colon = -1
        for (k in from + 1 until to) if (s[k] == ':') { colon = k; break }
        if (colon < 0) return
        val name = s.substring(from, to)
        if (colon + 1 == to || s.indexOf(':', colon + 1).let { it in 0 until to }) {
            fail(from, "Element or attribute \"$name\" do not match QName production: QName::=(NCName:)?NCName.")
        }
        if (element && colon - from == 5 && s.startsWith("xmlns", from)) {
            fail(from, "Element \"$name\" cannot have \"xmlns\" as its prefix.")
        }
    }

    /** End of a name starting at [from]; names end at white space, markup delimiters or the end of input. */
    private fun nameEnd(from: Int): Int {
        var k = from
        while (k < n) {
            val c = s[k]
            if (isXmlSpace(c) || c == '/' || c == '>' || c == '=' || c == '"' || c == '\'' || c == '<') break
            k++
        }
        return k
    }

    private fun recordColons(from: Int, to: Int) {
        if (namespaceAware) return
        for (k in from until to) if (s[k] == ':') nameColons.add(k)
    }

    private fun skipSpace(from: Int): Int {
        var k = from
        while (k < n && isXmlSpace(s[k])) k++
        return k
    }

    private fun endTag() {
        val start = i + 2
        val end = nameEnd(start)
        if (end == start) fail(start, "The end-tag must have a name.")
        checkName(start, end, "The end-tag name is not a legal XML name.")
        recordColons(start, end)
        val k = skipSpace(end)
        if (k >= n || s[k] != '>') fail(k, "The end-tag for element type \"" + s.substring(start, end) + "\" must end with a '>' delimiter.")
        i = k + 1
    }

    private fun startTag() {
        seenRoot = true
        val start = i + 1
        val end = nameEnd(start)
        if (end == start) fail(start, "The markup in the document must be well-formed (a start tag needs a name).")
        checkName(start, end, "The markup in the document must be well-formed (illegal element name).")
        checkQName(start, end, element = true)
        recordColons(start, end)
        val element = s.substring(start, end)
        var seen: MutableSet<String>? = null
        var k = end
        while (true) {
            val wsStart = k
            k = skipSpace(k)
            if (k >= n) fail(k, "XML document structures must start and end within the same entity.")
            val c = s[k]
            if (c == '>') { i = k + 1; return }
            if (c == '/') {
                if (k + 1 < n && s[k + 1] == '>') { i = k + 2; return }
                fail(k, "Element type \"$element\" must be followed by either attribute specifications, \">\" or \"/>\".")
            }
            if (k == wsStart) fail(k, "Element type \"$element\" must be followed by either attribute specifications, \">\" or \"/>\".")
            // attribute name
            val aStart = k
            val aEnd = nameEnd(k)
            if (aEnd == aStart) fail(k, "Element type \"$element\" must be followed by either attribute specifications, \">\" or \"/>\".")
            val aName = s.substring(aStart, aEnd)
            if (seen == null) seen = HashSet()
            if (!seen.add(aName)) fail(aStart, "Attribute \"$aName\" was already specified for element \"$element\".")
            checkName(aStart, aEnd, "Element type \"$element\" must be followed by either attribute specifications, \">\" or \"/>\".")
            checkQName(aStart, aEnd, element = false)
            recordColons(aStart, aEnd)
            if (!namespaceAware && aName == "xmlns") xmlnsNames.add(aStart + 4)
            k = skipSpace(aEnd)
            if (k >= n || s[k] != '=') fail(k, "Attribute name \"$aName\" associated with an element type \"$element\" must be followed by the ' = ' character.")
            k = skipSpace(k + 1)
            if (k >= n || (s[k] != '"' && s[k] != '\'')) fail(k, "Open quote is expected for attribute \"$aName\" associated with an element type \"$element\".")
            val quote = s[k]
            k++
            while (true) {
                if (k >= n) fail(k, "XML document structures must start and end within the same entity.")
                val v = s[k]
                when {
                    v == quote -> break
                    v == '<' -> fail(k, "The value of attribute \"$aName\" associated with an element type \"$element\" must not contain the '<' character.")
                    v == '&' && k + 1 < n && s[k + 1] == '#' -> k = charRef(k)
                    v == '&' && docType != null -> k = entityRef(k, true)
                    else -> k++
                }
            }
            k++
        }
    }

    /** Checks the character reference at [at] (`&#...;`) and returns the offset after its `;`. */
    private fun charRef(at: Int): Int {
        var k = at + 2
        val hex = k < n && s[k] == 'x'
        if (hex) k++
        val digitsStart = k
        var value = 0
        while (k < n) {
            val d = when (val c = s[k]) {
                in '0'..'9' -> c - '0'
                in 'a'..'f' -> if (hex) c - 'a' + 10 else -1
                in 'A'..'F' -> if (hex) c - 'A' + 10 else -1
                else -> -1
            }
            if (d < 0) break
            value = if (value > 0x10FFFF) value else value * (if (hex) 16 else 10) + d
            k++
        }
        if (k == digitsStart) {
            fail(k, if (hex) "A hexadecimal representation must immediately follow the \"&#x\" in a character reference."
                    else "A decimal representation must immediately follow the \"&#\" in a character reference.")
        }
        if (k >= n || s[k] != ';') fail(k, "The character reference must end with the ';' delimiter.")
        if (!isXmlCodePoint(value)) fail(k, "Character reference \"" + s.substring(at, k) + "\" is an invalid XML character.")
        if (value in 0xE000..0xF8FF) privateUseRefs.add(value)
        return k + 1
    }

    // ---- permissive mode: DOCTYPE and entity references -------------------------------------------------------------

    /** `<!DOCTYPE` S Name (S ExternalID)? S? (`[` intSubset `]` S?)? `>` at [i]; never reads the external subset. */
    private fun docTypeDeclaration() {
        val start = i
        if (seenRoot) fail(i, "The markup in the document following the root element must be well-formed.")
        if (docType != null) fail(i, "Already seen doctype.")
        var k = i + 9
        if (k >= n || !isXmlSpace(s[k])) fail(k, "White space is required after \"<!DOCTYPE\" in the document type declaration.")
        k = skipSpace(k)
        val nameStart = k
        while (k < n && !isXmlSpace(s[k]) && s[k] != '[' && s[k] != '>') k++
        if (k == nameStart) fail(k, "The root element type must appear after \"<!DOCTYPE\" in the document type declaration.")
        checkName(nameStart, k, "The markup in the document preceding the root element must be well-formed.")
        val name = s.substring(nameStart, k)
        var afterName = skipSpace(k)
        var publicId: String? = null
        var systemId: String? = null
        if (afterName > k && (s.startsWith("PUBLIC", afterName) || s.startsWith("SYSTEM", afterName))) {
            val ids = externalId(afterName, requireSystem = true)
            publicId = ids.publicId
            systemId = ids.systemId
            afterName = skipSpace(ids.end)
        }
        k = afterName
        val items = ArrayList<DtdItem>()
        val entities = HashMap<String, DtdItem.Entity>()
        if (k < n && s[k] == '[') {
            k = internalSubset(k + 1, items, entities)
            k = skipSpace(k)
        }
        if (k >= n || s[k] != '>') fail(k, "The document type declaration for root element type \"$name\" must end with '>'.")
        i = k + 1
        docType = ScannedDocType(name, publicId, systemId, start, i, items, entities)
    }

    private class Ids(val publicId: String?, val systemId: String?, val end: Int)

    /** `SYSTEM` S SystemLiteral | `PUBLIC` S PubidLiteral (S SystemLiteral)? at [from] ([requireSystem]: the second literal is required). */
    private fun externalId(from: Int, requireSystem: Boolean): Ids {
        val public = s.startsWith("PUBLIC", from)
        var k = from + 6
        if (k >= n || !isXmlSpace(s[k])) fail(k, "White space is required after keyword " + (if (public) "PUBLIC" else "SYSTEM") + " in DOCTYPE decl.")
        k = skipSpace(k)
        var publicId: String? = null
        if (public) {
            val lit = literal(k)
            for (p in lit.first until lit.second) if (!isPubidChar(s[p])) {
                fail(p, "An invalid XML character (Unicode: 0x" + s[p].code.toString(16) + ") was found in the public identifier.")
            }
            publicId = normalizePublicId(s.substring(lit.first, lit.second))
            k = lit.second + 1
            val afterSpace = skipSpace(k)
            if (afterSpace < n && (s[afterSpace] == '"' || s[afterSpace] == '\'')) {
                if (afterSpace == k) fail(k, "White spaces are required between publicId and systemId.")
            } else {
                if (requireSystem) fail(afterSpace, "White spaces are required between publicId and systemId.")
                return Ids(publicId, null, k)
            }
            k = afterSpace
        }
        val lit = literal(k)
        return Ids(publicId, normalizeLineEnds(s.substring(lit.first, lit.second)), lit.second + 1)
    }

    /** A quoted literal at [from]: `[contentStart, closingQuote)`. */
    private fun literal(from: Int): Pair<Int, Int> {
        if (from >= n || (s[from] != '"' && s[from] != '\'')) fail(from, "The quoted literal is missing in the document type declaration.")
        val end = s.indexOf(s[from], from + 1)
        if (end < 0) fail(n, "XML document structures must start and end within the same entity.")
        return Pair(from + 1, end)
    }

    private fun isPubidChar(c: Char): Boolean =
        c == ' ' || c == '\r' || c == '\n' || c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c in "-'()+,./:=?;!*#@\$_%"

    /** The internal subset from [from] (after `[`) to its `]`; returns the offset after `]`. */
    private fun internalSubset(from: Int, items: MutableList<DtdItem>, entities: MutableMap<String, DtdItem.Entity>): Int {
        val parameterEntities = HashSet<String>()
        var k = from
        while (true) {
            k = skipSpace(k)
            if (k >= n) fail(n, "XML document structures must start and end within the same entity.")
            when {
                s[k] == ']' -> return k + 1
                s.startsWith("<!--", k) -> {
                    val end = s.indexOf("-->", k + 4)
                    if (end < 0) fail(n, "XML document structures must start and end within the same entity.")
                    val text = s.substring(k + 4, end)
                    if (text.contains("--") || text.endsWith("-")) fail(k, "The string \"--\" is not permitted within comments.")
                    items.add(DtdItem.Comment(normalizeLineEnds(text)))
                    k = end + 3
                }
                s.startsWith("<?", k) -> {
                    val tStart = k + 2
                    var t = tStart
                    while (t < n && !isXmlSpace(s[t]) && !s.startsWith("?>", t)) t++
                    if (t == tStart || t >= n) fail(t, "The processing instruction must begin with the name of the target.")
                    checkName(tStart, t, "White space is required between the processing instruction target and data.")
                    val target = s.substring(tStart, t)
                    if (target.equals("xml", ignoreCase = true)) fail(tStart, "The processing instruction target matching \"[xX][mM][lL]\" is not allowed.")
                    val end = s.indexOf("?>", t)
                    if (end < 0) fail(n, "XML document structures must start and end within the same entity.")
                    items.add(DtdItem.Pi(target, normalizeLineEnds(s.substring(skipSpace(t).coerceAtMost(end), end))))
                    k = end + 2
                }
                s.startsWith("<!ENTITY", k) -> k = entityDeclaration(k, items, entities, parameterEntities)
                s.startsWith("<!ELEMENT", k) -> {
                    var p = k + 9
                    if (p >= n || !isXmlSpace(s[p])) fail(p, "White space is required after \"<!ELEMENT\" in the element type declaration.")
                    p = skipSpace(p)
                    val nStart = p
                    while (p < n && !isXmlSpace(s[p]) && s[p] != '>' && s[p] != '(') p++
                    if (p == nStart) fail(p, "The element type is required in the element type declaration.")
                    checkName(nStart, p, "The element type is required in the element type declaration.")
                    val name = s.substring(nStart, p)
                    if (p >= n || !isXmlSpace(s[p])) fail(p, "White space is required after the element type \"$name\" in the element type declaration.")
                    val specStart = skipSpace(p)
                    val specEnd = contentSpec(specStart, name)
                    val end = skipSpace(specEnd)
                    if (end >= n || s[end] != '>') fail(end, "The declaration for element type \"$name\" must end with '>'.")
                    // SAX's DeclHandler reports the content model with its white space removed
                    items.add(DtdItem.ElementDecl(name, s.substring(specStart, specEnd).filterNot { isXmlSpace(it) }))
                    k = end + 1
                }
                s.startsWith("<!NOTATION", k) -> {
                    var p = k + 10
                    if (p >= n || !isXmlSpace(s[p])) fail(p, "White space is required after \"<!NOTATION\" in the notation declaration.")
                    p = skipSpace(p)
                    val nStart = p
                    while (p < n && !isXmlSpace(s[p]) && s[p] != '>') p++
                    if (p == nStart) fail(p, "The notation name is required in the notation declaration.")
                    checkName(nStart, p, "The notation name is required in the notation declaration.")
                    val name = s.substring(nStart, p)
                    val q = skipSpace(p)
                    if (q == p || !(s.startsWith("PUBLIC", q) || s.startsWith("SYSTEM", q))) {
                        fail(q, "The external identifier is required in the declaration for notation \"$name\".")
                    }
                    val ids = externalId(q, requireSystem = false)
                    val end = skipSpace(ids.end)
                    if (end >= n || s[end] != '>') fail(end, "The declaration for notation \"$name\" must end with '>'.")
                    items.add(DtdItem.Notation(name, ids.publicId, ids.systemId))
                    k = end + 1
                }
                s.startsWith("<!ATTLIST", k) ->
                    fail(k, "kdom does not support attribute-list declarations in a DOCTYPE internal subset (default attribute values would change the tree).")
                s[k] == '%' -> fail(k, "kdom does not support parameter-entity references in a DOCTYPE internal subset.")
                else -> fail(k, "The markup declarations contained or pointed to by the document type declaration must be well-formed.")
            }
        }
    }

    /**
     * `contentspec ::= 'EMPTY' | 'ANY' | Mixed | children` at [k] (XML 1.0 [46]-[51]); returns the offset after it. A
     * parameter-entity reference in it is rejected as unsupported.
     */
    private fun contentSpec(k: Int, element: String): Int {
        fun bad(at: Int): Nothing = fail(at, "The content model of element type \"$element\" is malformed.")
        fun nameEnd(from: Int): Int {
            var e = from
            while (e < n && !isXmlSpace(s[e]) && s[e] !in "()|,?*+>%") e++
            if (e == from) bad(from)
            checkName(from, e, "The content model of element type \"$element\" is malformed.")
            return e
        }
        fun quantifier(at: Int) = if (at < n && s[at] in "?*+") at + 1 else at
        // cp ::= (Name | choice | seq) ('?' | '*' | '+')?
        fun cp(from: Int, group: (Int) -> Int): Int = quantifier(if (from < n && s[from] == '(') group(from) else nameEnd(from))
        // choice | seq: '(' S? cp ( S? ('|' | ',') S? cp )* S? ')' with one separator kind
        fun group(from: Int): Int {
            var p = skipSpace(from + 1)
            p = cp(p, ::group)
            var sep = '\u0000'
            while (true) {
                p = skipSpace(p)
                if (p >= n) bad(p)
                val c = s[p]
                if (c == ')') return p + 1
                if (c != '|' && c != ',') bad(p)
                if (sep != '\u0000' && c != sep) bad(p)
                sep = c
                p = cp(skipSpace(p + 1), ::group)
            }
        }
        if (k < n && s[k] == '%') fail(k, "kdom does not support parameter-entity references in a DOCTYPE internal subset.")
        if (s.startsWith("EMPTY", k)) return k + 5
        if (s.startsWith("ANY", k)) return k + 3
        if (k >= n || s[k] != '(') bad(k)
        val first = skipSpace(k + 1)
        if (s.startsWith("#PCDATA", first)) {
            // Mixed ::= '(' S? '#PCDATA' (S? '|' S? Name)* S? ')*' | '(' S? '#PCDATA' S? ')'
            var p = first + 7
            var names = 0
            while (true) {
                p = skipSpace(p)
                if (p >= n) bad(p)
                if (s[p] == ')') break
                if (s[p] != '|') bad(p)
                p = nameEnd(skipSpace(p + 1))
                names++
            }
            p++
            if (p < n && s[p] == '*') return p + 1
            if (names > 0) bad(p)
            return p
        }
        return quantifier(group(k))
    }

    private fun entityDeclaration(
        at: Int,
        items: MutableList<DtdItem>,
        entities: MutableMap<String, DtdItem.Entity>,
        parameterEntities: MutableSet<String>,
    ): Int {
        var p = at + 8
        if (p >= n || !isXmlSpace(s[p])) fail(p, "White space is required after \"<!ENTITY\" in the entity declaration.")
        p = skipSpace(p)
        var parameter = false
        if (p < n && s[p] == '%') {
            parameter = true
            p++
            if (p >= n || !isXmlSpace(s[p])) fail(p, "White space is required before the entity name in the parameter entity declaration.")
            p = skipSpace(p)
        }
        val nStart = p
        while (p < n && !isXmlSpace(s[p]) && s[p] != '>' && s[p] != '"' && s[p] != '\'') p++
        if (p == nStart) fail(p, "The entity name is required in the entity declaration.")
        checkName(nStart, p, "The entity name is required in the entity declaration.")
        val name = s.substring(nStart, p)
        val q = skipSpace(p)
        if (q == p) fail(q, "White space is required between the entity name \"$name\" and the definition in the entity declaration.")
        val decl: DtdItem.Entity
        var end: Int
        if (q < n && (s[q] == '"' || s[q] == '\'')) {
            val lit = literal(q)
            decl = DtdItem.Entity(if (parameter) "%$name" else name, entityValue(lit.first, lit.second), null, null, null)
            end = lit.second + 1
        } else {
            if (!(s.startsWith("PUBLIC", q) || s.startsWith("SYSTEM", q))) fail(q, "The entity value or external identifier is required for entity \"$name\".")
            val ids = externalId(q, requireSystem = true)
            end = ids.end
            var notation: String? = null
            val r = skipSpace(end)
            if (!parameter && s.startsWith("NDATA", r)) {
                if (r == end) fail(r, "White space is required before NDATA.")
                var t = r + 5
                if (t >= n || !isXmlSpace(s[t])) fail(t, "White space is required after NDATA.")
                t = skipSpace(t)
                val nn = t
                while (t < n && !isXmlSpace(s[t]) && s[t] != '>') t++
                if (t == nn) fail(t, "The notation name is required after NDATA.")
                checkName(nn, t, "The notation name is required after NDATA.")
                notation = s.substring(nn, t)
                end = t
            }
            decl = DtdItem.Entity(if (parameter) "%$name" else name, null, ids.publicId, ids.systemId, notation)
        }
        end = skipSpace(end)
        if (end >= n || s[end] != '>') fail(end, "The declaration for the entity \"$name\" must end with '>'.")
        val first = if (parameter) parameterEntities.add(name) else !entities.containsKey(name)
        if (first) {
            if (!parameter) entities[name] = decl
            items.add(decl)
        }
        return end + 1
    }

    /** Replacement text of an entity value literal `s[from, to)`: character references expanded, general entity references kept. */
    private fun entityValue(from: Int, to: Int): String {
        val sb = StringBuilder()
        var k = from
        while (k < to) {
            val c = s[k]
            when {
                c == '%' -> fail(k, "kdom does not support parameter-entity references in a DOCTYPE internal subset.")
                c == '&' && k + 1 < to && s[k + 1] == '#' -> {
                    val after = charRef(k)
                    sb.append(charRefText(k, after))
                    k = after
                }
                c == '&' -> {
                    val semi = s.indexOf(';', k)
                    if (semi < 0 || semi >= to || semi == k + 1) fail(k, "The reference to an entity must end with the ';' delimiter.")
                    checkName(k + 1, semi, "The entity name must immediately follow the '&' in the entity reference.")
                    sb.append(s, k, semi + 1)
                    k = semi + 1
                }
                else -> { sb.append(c); k++ }
            }
        }
        return normalizeLineEnds(sb.toString())
    }

    /** The character of the (already validated) character reference `s[from, after)`. */
    private fun charRefText(from: Int, after: Int): String {
        val hex = s[from + 2] == 'x'
        val digits = s.substring(from + (if (hex) 3 else 2), after - 1)
        val cp = digits.toInt(if (hex) 16 else 10)
        return if (cp >= 0x10000) {
            val v = cp - 0x10000
            charArrayOf((0xD800 + (v shr 10)).toChar(), (0xDC00 + (v and 0x3FF)).toChar()).concatToString()
        } else cp.toChar().toString()
    }

    /**
     * A non-predefined entity reference `&name;` at [at] in content ([inAttribute] false) or an attribute value, in a document
     * with a DOCTYPE (permissive mode). As the JDK parser with `load-external-dtd` off: an internal entity is expanded; an
     * external parsed entity is skipped in content and an error in an attribute value; an undeclared entity is skipped (in
     * content) or dropped (in an attribute value) when the DOCTYPE has an external id that was not read and the document
     * is not `standalone="yes"`, else it is an error. Returns the offset after `;`.
     */
    private fun entityRef(at: Int, inAttribute: Boolean): Int {
        val semi = s.indexOf(';', at)
        val nameEnd = nameEnd(at + 1).let { e -> if (semi in (at + 1) until e) semi else e }
        if (nameEnd == at + 1) fail(at + 1, "The entity name must immediately follow the '&' in the entity reference.")
        checkName(at + 1, nameEnd, "The entity name must immediately follow the '&' in the entity reference.")
        val name = s.substring(at + 1, nameEnd)
        if (nameEnd >= n || s[nameEnd] != ';') fail(nameEnd, "The reference to entity \"$name\" must end with the ';' delimiter.")
        if (name in PREDEFINED) return nameEnd + 1
        val dt = docType!!
        val decl = dt.entities[name]
        val use: EntityUse = when {
            decl == null -> {
                if (dt.systemId == null && dt.publicId == null || standaloneYes) {
                    fail(at, "The entity \"$name\" was referenced, but not declared.")
                }
                if (inAttribute) EntityUse.Expand("") else EntityUse.Skip(name)
            }
            decl.notation != null -> fail(at, "The unparsed entity reference \"&$name;\" is not permitted.")
            decl.value == null -> {
                if (inAttribute) fail(at, "The external entity reference \"&$name;\" is not permitted in an attribute value.")
                EntityUse.Skip(name)
            }
            else -> {
                val text = expandReplacementText(decl.value, at, name, inAttribute)
                EntityUse.Expand(if (inAttribute) text.map { if (it == '\t' || it == '\n' || it == '\r') ' ' else it }.joinToString("") else text)
            }
        }
        entityRefs.add(intArrayOf(at, nameEnd + 1, entityUses.size))
        entityUses.add(use)
        return nameEnd + 1
    }

    /** Replacement text of an internal entity as character data; markup or nested entity references are not supported. */
    private fun expandReplacementText(value: String, at: Int, name: String, inAttribute: Boolean): String {
        // In content the replacement text is parsed as content: "]]>" there is an error, as in SAX.
        if (!inAttribute && value.contains("]]>")) {
            fail(at, "The character sequence \"]]>\" must not appear in content unless used to mark the end of a CDATA section.")
        }
        if (!value.contains('&') && !value.contains('<')) return value
        if (value.contains('<')) fail(at, "kdom does not support entity \"$name\": its replacement text contains markup.")
        val sb = StringBuilder()
        var k = 0
        while (k < value.length) {
            val c = value[k]
            if (c != '&') { sb.append(c); k++; continue }
            val semi = value.indexOf(';', k)
            if (semi < 0) fail(at, "XML document structures must start and end within the same entity (in entity \"$name\").")
            when (val ref = value.substring(k + 1, semi)) {
                "lt" -> sb.append('<')
                "gt" -> sb.append('>')
                "amp" -> sb.append('&')
                "apos" -> sb.append('\'')
                "quot" -> sb.append('"')
                else -> fail(at, "kdom does not support entity \"$name\": its replacement text references entity \"$ref\".")
            }
            k = semi + 1
        }
        return sb.toString()
    }

    private fun fail(at: Int, message: String): Nothing {
        val lc = lineColumnOf(s, at)
        throw SAXParseException(message, lc[0], lc[1])
    }

    private companion object {
        val PREDEFINED = setOf("lt", "gt", "amp", "apos", "quot")
        val STANDALONE_YES = Regex("standalone[ \t\r\n]*=[ \t\r\n]*([\"'])yes\\1")

        fun normalizeLineEnds(v: String): String = if (v.indexOf('\r') < 0) v else v.replace("\r\n", "\n").replace('\r', '\n')

        /** SAX normalizes a public id: white space runs become one space, leading/trailing white space is removed. */
        fun normalizePublicId(v: String): String = v.split(' ', '\t', '\r', '\n').filter { it.isNotEmpty() }.joinToString(" ")
    }
}

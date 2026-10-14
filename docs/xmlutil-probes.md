# xmlutil 1.0.2.1 reader probes

Pinned by `src/commonTest/.../probe/XmlUtilReaderProbeTest.kt` (common: JVM run + iOS compile), inputs shared via `ProbeInputs.kt`. The SAX column is pinned
by `src/jvmTest/.../probe/SaxReferenceProbeTest.kt` (JAXP `SAXParserFactory`, default JDK parser).

Reader under test: `nl.adaptivity.xmlutil.xmlStreaming.newGenericReader(String)` (`XmlStreaming.newGenericReader(CharSequence/String, Boolean)`
in the 1.0.2.1 jar; the brief's name is correct). It is the KtXmlReader-backed common implementation. Every event is consumed
until `hasNext()` is false. "SAX ns" = namespace-aware (OSIS/GBF path), "SAX no-ns" = namespace-unaware (ThML path).

| # | Probe | Input | SAX ns | SAX no-ns | xmlutil 1.0.2.1 | XmlEventParser (Task 9) must |
|---|-------|-------|--------|-----------|-----------------|------------------------------|
| 1 | unclosed root | `<a><b></b>` | reject | reject | reject XmlException ("End of document before end of document element") | nothing |
| 2 | stray ampersand | `<a>x & y</a>` | reject | reject | reject XmlException (thrown while reading the TEXT after `x `) | nothing |
| 3 | undefined entity | `<a>&nbsp;</a>` | reject | reject | **accepted**: emits `ENTITY_REF` with empty text, no error | **P1** |
| 4 | unquoted attribute | `<a b=1/>` | reject | reject | reject XmlException | nothing |
| 5 | unterminated attr value | `<a b="1/>` | reject | reject | reject **IllegalStateException** ("Unexpected EOF") | map non-XmlException (P3) |
| 6 | mis-nested tags | `<a><b><c></b></c></a>` | reject | reject | reject XmlException | nothing |
| 7 | stray end tag | `<a></b></a>` | reject | reject | reject XmlException | nothing |
| 8 | text after root | `<a/>tail` | reject | reject | reject **IllegalStateException** ("Unexpected EOF") | map non-XmlException (P3) |
| 9 | two roots | `<a/><b/>` | reject | reject | reject XmlException ("Unexpected start tag after document body") | nothing |
| 10 | duplicate attribute | `<a b="1" b="2"/>` | reject | reject | **accepted**: START_ELEMENT reports both `b=1` and `b=2` | **P2** |
| 11 | less-than in text | `<a>1 < 2</a>` | reject | reject | reject XmlException | nothing |
| 12 | empty input | (empty) | reject | reject | reject XmlException ("End of document before end of document element") | nothing |
| 13 | undeclared prefix | `<p:a/>` | reject | **accepted** | reject XmlException ("undefined prefix: p") | **P4** (reader is always namespace-aware) |
| 14 | valid control | `<a x="&lt;&amp;&#233;"><![CDATA[<raw>]]>t&gt;</a>` | accept | accept | accept; event stream below | parse correctly |
| 15 | undefined entity in attribute | `<a x="&nbsp;"/>` | reject | reject | reject XmlException ("Unknown entity ... in entity expanding mode") | nothing (xmlutil already rejects; note the asymmetry with probe 3) |
| 16 | illegal character | `<a>\u0001</a>` | reject | reject | **accepted**: `TEXT('\u0001')` | **P5** |
| 17 | less-than in attribute | `<a x="<"/>` | reject | reject | **accepted**: attribute value `<` | **P6** |
| 18 | DOCTYPE with internal entity decl | `<!DOCTYPE a [<!ENTITY e "v">]><a>&e;</a>` | accepts (expands `&e;`) | accepts | **accepted**: `DOCDECL`, then `ENTITY_REF('v')` (expanded) | **P7** (plan rule, not SAX parity) |
| 19 | plain DOCTYPE | `<!DOCTYPE a><a/>` | accepts | accepts | **accepted**: `DOCDECL` event | **P7** |
| 20 | out-of-range character reference | `<a>&#1114112;</a>` | reject | reject | **accepted**: `ENTITY_REF` with a lone-surrogate garbage string | **P8** |

Note: the SAX (JDK) parser accepts DOCTYPE; rejecting it is the plan's rule for Task 9, so probes 18 and 19 are not SAX-parity
requirements. Also observed: comments and whitespace after the root element are accepted by both (xmlutil: IGNORABLE_WHITESPACE/COMMENT events).

## Event stream (pinned by `XmlUtilReaderProbeTest.eventSequences`)

- Every entity reference in element content, predefined (`&lt; &amp; &gt; &apos; &quot;`) or numeric (`&#233;`, `&#x41;`), arrives as an
  `ENTITY_REF` event whose `text` is the already resolved character (`<`, `&`, `\u00E9`, `A`, ...). They are NOT merged into
  the surrounding `TEXT`: `<a>p&nbsp;q</a>` is `TEXT('p') ENTITY_REF('') TEXT('q')`, and `t&gt;` is `TEXT('t') ENTITY_REF('>')`.
  The parser must therefore concatenate adjacent TEXT / ENTITY_REF / CDSECT text (as SAX characters).
- An undefined entity is an `ENTITY_REF` with **empty** text (the entity name is not exposed).
- CDATA is its own event type `CDSECT` with the raw content (`CDSECT('<raw>')`).
- In attribute values, refs are resolved already (`x="&lt;&amp;&#233;"` is `x='<&\u00E9'`); an undefined one throws XmlException.
- A DOCTYPE arrives as a `DOCDECL` event (its `text` throws XmlException "The element is not text"); internal-subset entities are
  expanded afterwards as `ENTITY_REF('v')`.
- Comments and whitespace after the root: `COMMENT`, `IGNORABLE_WHITESPACE`.

## Exceptions and locations (P3 input for JDOMParseException)

Messages are not pinned by the tests (class names only). Observed shapes: `XmlException` messages are prefixed `line:col - ` (for example
`1:11 - End of document before end of document element`). `XmlException.locationInfo` (type `XmlReader.LocationInfo`, `toString()` is
`line:col`) is present for every XmlException probe; the exception for reading `.text` of a DOCDECL says "Unknown position" in its
message, but the reader reports a position (1:13 for the plain DOCTYPE). The two `IllegalStateException` rejections (unterminated attribute value, text after root,
message "Unexpected EOF") carry no location; read it from the reader after the throw: `reader.extLocationInfo` (1:10 and 1:9 here). So
for P3 the mapping always uses `reader.extLocationInfo` (available for every rejection case probed) rather than the exception. The location
is where the reader stopped (generally the end of the offending token), not necessarily its start.

## Comparison with the K2 spike (xmlutil 0.91.3)

The spike's "unresolved entity dropped silently" and "duplicate attribute kept the last value" were effects of the spike's builder at
0.91.3, not the reader alone; the spike's rejects were measured after three builder patches. Reader-level facts for 1.0.2.1: the
undefined entity is an `ENTITY_REF` event with empty text (detectable); the duplicate attribute is reported twice by the reader
(no last-wins inside the reader); empty input throws `XmlException`. Two rejections (unterminated attribute value, text after root)
surface as `IllegalStateException`, not `XmlException`.

## Requirements for XmlEventParser (Task 9)

- **P1 (undefined entity):** every entity reference is an `ENTITY_REF` event (see above), so the event type alone does not identify
  an undefined one. Rule: an `ENTITY_REF` whose `text` is empty is an undefined entity and must raise a parse error (SAX rejects
  `&nbsp;`). Valid references never have empty text (probe 14 and the content-refs sequence). Everything else (`TEXT`, `ENTITY_REF`,
  `CDSECT`) is character data, concatenated. A reference inside an attribute value needs no check (xmlutil throws, probe 15).
- **P2 (duplicate attribute):** on every START_ELEMENT, detect two attributes with the same (namespace, local name) (same qualified
  name in ns-unaware mode) and raise a parse error.
- **P3 (exception mapping):** catch both `XmlException` and `IllegalStateException` (in practice any `Exception` from the reader) and
  rethrow as `JDOMParseException` with line/column from `reader.extLocationInfo`; no raw exception may leak from `KdomBuilder`.
- **P4 (namespace-unaware ThML):** SAX no-ns accepts `<p:a/>`; ThML relies on that. xmlutil's generic reader is always
  namespace-aware and rejects it. A namespace-unaware mode is needed (for example pre-masking the prefix colon, or tolerating the
  "undefined prefix" XmlException); namespace-aware (OSIS/GBF) keeps rejecting (already does).
- **P5 (illegal character):** reject any character in text, attribute values and CDATA outside the XML 1.0 `Char` production
  (`#x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]`); xmlutil passes `\u0001` through. Check at least TEXT
  and CDSECT payloads and attribute values.
- **P6 (less-than in attribute value):** reject a raw `<` in an attribute value (xmlutil accepts it). Since values are delivered
  already resolved, a legal `&lt;` is indistinguishable from a raw `<`; the check needs the raw source (scan the attribute text of the
  start tag, or position-based), or the requirement is relaxed explicitly in Task 9 with a test noting the deviation.
- **P7 (DOCTYPE):** reject any `DOCDECL` event (plan rule: Task 9 rejects every DOCTYPE; xmlutil would expand internal entities,
  so `&e;` must never reach the tree). Covers probes 18 and 19.
- **P8 (out-of-range character reference):** reject an `ENTITY_REF` that came from a numeric reference above U+10FFFF or an
  illegal code point (xmlutil yields a garbage surrogate string); simplest is to apply P5 to the `ENTITY_REF` text, together with a
  lone-surrogate check.
- No requirement for probes 1, 2, 4, 6, 7, 9, 11, 12, 15: xmlutil rejects them, as SAX does.

## How XmlEventParser (Task 9) meets P1..P8

`fi.sykero.kdom.input.XmlEventParser` reads with `xmlStreaming.newGenericReader(text, expandEntities = true)` after a raw-source
pre-scan (`SourceScanner`) and, when needed, a same-length masked copy of the text (`XmlSourceMask`). Pinned by
`XmlEventParserProbeTest` (common, all 20 probes in both modes: SAX verdicts, DOCTYPE probes rejected),
`SaxEventDifferentialTest` (jvmTest: event streams against the JDK SAX parser for 31 well-formed inputs in both modes, 75
malformed inputs, 11 mode-specific inputs) and `KdomBuilderDifferentialTest` (trees against jdom2's `SAXBuilder`).

- **P1** undefined entity: the reader runs in entity-expanding mode, which throws for an unknown entity (text and attributes).
- **P2** duplicate attribute: the pre-scan rejects a repeated raw qualified name in a start tag (this also covers a repeated
  `xmlns:p`, which the reader accepts); in namespace-aware mode the event loop also rejects two attributes with the same
  (namespace URI, local name) (`p:x` and `q:x` bound to one URI).
- **P3** exceptions: every reader call runs inside a guard that turns any `Exception` (XmlException, IllegalStateException, ...)
  into `fi.sykero.kdom.sax.SAXParseException` with line/column from `reader.extLocationInfo` (`XmlReader.ExtLocationInfo`, the
  `line:col - ` prefix stripped from the message). Exceptions thrown by the handler propagate unchanged, as with SAX.
- **P4** namespace-unaware mode: xmlutil 1.0.2.1 has no namespace-unaware option (`KtXmlReader.PROCESS_NAMESPACES` is a constant
  `true`; the only switch, `relaxed`, also drops well-formedness errors, so it is not used). The pre-scan records every `:` in
  element/attribute names and every attribute named exactly `xmlns`; the masked copy replaces each such `:` by a name character
  absent from the document (first free one in U+2C00..U+2FEF) and the `s` of `xmlns` by another one, so the reader sees plain
  names and no namespace declarations. The event loop restores the names and reports SAX's no-namespace shape: element
  `uri = ""`, `localName = ""`, `qName` = raw name; attributes `uri = ""`, `localName = qName` (what the JDK parser reports),
  `xmlns*` as ordinary attributes, no prefix mappings. Line/column positions are unchanged (same length).
- **P5** illegal characters: the pre-scan checks every character of the document (text, attribute values, CDATA, comments,
  processing instructions, names) against the XML 1.0 `Char` production, with surrogates paired, as SAX does.
- **P6** `<` in an attribute value (ruling R7): the pre-scan walks start tags (skipping comments, CDATA sections and processing
  instructions) and rejects a literal `<` inside a quoted attribute value. Byte input is decoded first, then scanned.
- **P7** DOCTYPE: the pre-scan rejects `<!DOCTYPE` (and every other `<!` declaration outside a comment/CDATA); a `DOCDECL`
  event is rejected as well.
- **P8** character references: the pre-scan checks each `&#...;` / `&#x...;` in text and attribute values: digits present,
  `;` present, and the code point a legal `Char` (`&#0;`, `&#xD800;`, `&#55357;&#56832;`, `&#1114112;` are rejected).

Further SAX parity fixes found by the differential test (xmlutil accepts or differs, SAX does not):
- `]]>` in character data, attributes not separated by white space (`<a x="1"y="2"/>`), a leading U+FEFF in a string,
  `<?xml version="2.0"?>`: rejected (pre-scan / START_DOCUMENT check; versions 1.0 and 1.1 are accepted).
- U+0085 and U+2028: xmlutil applies XML 1.1 line-end handling (turns them into `\n` in text and a space in attribute
  values); SAX 1.0 keeps them. They are masked as private-use characters absent from the document (and from its character
  references) and restored in every reported string.
- Processing instruction data: SAX skips all white space after the target (`<?pi\nd?>` is target `pi`, data `d`); xmlutil's
  `piTarget`/`piData` split at the first space only. The parser splits the PI text itself.
- `xmlns:xmlns="..."`, a prefix bound to the XML namespace or `xml` bound elsewhere: rejected in namespace-aware mode; the
  predeclared `xmlns:xml="http://www.w3.org/XML/1998/namespace"` is accepted and, as by SAX, not reported as a prefix mapping.

Task 9 fix round 1 (found by the reviewer's mutation differential, ~209k comparisons against the JDK parser):
- **Lone CR**: xmlutil folds `\r\r` into one line end (SAX: two). The pre-scan records every CR not followed by LF and the
  masked copy holds the LF it normalizes to (same length, so lines and offsets stay); attribute values then get a space each,
  as with SAX. Covers text, CDATA, comments, PI data, attribute values and namespace URIs.
- **PI target**: must be an XML Name followed by white space or `?>` (`<?b,c?>`, `<?p"d?>`, `<?p=?>`, `<??>`, `<?1?>` are
  rejected; `<?p:i d?>` is accepted, as by the JDK; jdom2's tree layer then rejects the colon, see KdomBuilder).
- **XML declaration**: white space is required after each quoted pseudo-attribute value (`version="1.0"encoding=...`).
- **Names**: element, attribute and end-tag names are checked with the XML 1.0 4th-edition tables the JDK parser uses
  (xmlutil uses the 5th-edition ones and accepts e.g. U+037E, U+2C01, U+0F00 in names). kdom's `Verifier.isXMLNameStartCharacter`
  / `isXMLNameCharacter` (jdom2's tables) are identical to the JDK's `XMLChar.isNameStart` / `isName` on all 65536 BMP
  characters (checked once with a scratch program against `com.sun.org.apache.xerces.internal.util.XMLChar`); supplementary
  characters are never name characters (as in the JDK's XML 1.0 scanner).
- **QName (namespace-aware)**: the first `:` after the first character splits prefix and local part; an empty local part or
  a further `:` is rejected (`p::b`, `b:`, `xmlns::p`), and so is the element prefix `xmlns`. A leading `:` belongs to the local
  name (`<:a/>` is accepted, as by the JDK).
- **Namespace URIs**: the U+0085/U+2028 masks are undone in namespace declarations and element/attribute namespace URIs.

Remaining accept/reject divergences of the differential (seeds 7/11/23, 143k comparisons): DOCTYPE (plan rule P7), an XML
declaration whose `encoding` value contains `&`, `<` or `>` (the JDK ignores the name when reading a `Reader`; xmlutil rejects
the declaration), and the byte-input encodings listed in translation-notes.md.

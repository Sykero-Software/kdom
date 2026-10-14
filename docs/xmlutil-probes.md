# xmlutil 1.0.2.1 reader probes

Pinned by `src/commonTest/.../probe/XmlUtilReaderProbeTest.kt` (common: JVM run + iOS compile). The SAX column is pinned
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
| 14 | valid control | `<a x="&lt;&amp;&#233;"><![CDATA[<raw>]]>t&gt;</a>` | accept | accept | accept | parse correctly |

Also observed: comments and whitespace after the root element are accepted by both (xmlutil: IGNORABLE_WHITESPACE/COMMENT events).

## Comparison with the K2 spike (xmlutil 0.91.3)

Spike (`status/2026-10-09-k2-xml-spike.md` § Malformation probes): unresolved entity dropped silently, duplicate attribute kept the last
value, empty input threw `IllegalArgumentException`. In 1.0.2.1: the undefined entity is still not an error, but is now visible as an
`ENTITY_REF` event (empty text) so it can be detected; the duplicate attribute is surfaced (both attributes reported, no silent
last-wins inside the reader); empty input now throws `XmlException` rather than `IllegalArgumentException`. Two rejections
(unterminated attribute value, text after root) surface as `IllegalStateException`, not `XmlException`.

## Requirements for XmlEventParser (Task 9)

Probes xmlutil accepts (or handles differently) but SAX rejects or accepts:

- **P1 (undefined entity):** an `ENTITY_REF` event whose entity is not one of the five predefined ones or a numeric character
  reference must raise a parse error (SAX rejects `&nbsp;`). Verify at Task 9 that predefined/numeric refs are delivered as TEXT
  and not as ENTITY_REF (probe 14 shows `&lt; &amp; &#233; &gt;` accepted without issue).
- **P2 (duplicate attribute):** on every START_ELEMENT, detect two attributes with the same (namespace, local name) (or the same
  qualified name in ns-unaware mode) and raise a parse error (SAX rejects; xmlutil reports both).
- **P3 (exception mapping):** xmlutil raises both `XmlException` and `IllegalStateException` (EOF cases). The parser must catch both
  (and in practice any `Exception` from the reader) and rethrow as `JDOMParseException`/`JDOMException`, so that no raw
  `IllegalStateException` leaks from `KdomBuilder`.
- **P4 (namespace-unaware ThML):** SAX no-ns accepts `<p:a/>`; ThML content relies on that (undeclared prefixes). xmlutil's generic
  reader is always namespace-aware and rejects it. XmlEventParser needs a namespace-unaware mode for the ThML path (for example
  pre-masking the prefix colon, or tolerating the "undefined prefix" XmlException when namespace-unaware) and must not
  reject undeclared prefixes there; with namespace-aware (OSIS/GBF) it must keep rejecting them (already does).
- No requirement for probes 1, 2, 4, 6, 7, 9, 11, 12: xmlutil rejects them, as SAX does.

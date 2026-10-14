# Translation notes

Differences between kdom and jdom2 2.0.6.1. Every intended behavioural or API difference gets a test
and a line here.

## Property rule exceptions

(none yet)

## Behaviour differences

(none yet)

## Task 3: jdom2 subset import (Java)

Source: hunterhacker/jdom tag `JDOM-2.0.6.1` (commit c6065f684bdff5cebf5f23b2859c5e4c89fd89db), imported by
`scripts/import-jdom2.py` (58 main classes, 40 test classes). The script is the provenance record; the three
test files listed under "Removed test methods" / "Ignored tests" were hand-edited afterwards, so re-running the
script overwrites those edits.

### Scope deviation

- `filter/ContentFilter` is the 58th class (the brief lists 57). It is deprecated in jdom2 but jdom2's own
  `TestContentFilter`, `AbstractTestFilter`, `TestFilters`, `TestDocument` and `TestElement` use it, so it is
  imported unchanged. JSword and `:app` do not need it.

### Removed members

- `JDOMConstants`: four javadoc-only imports (`javax.xml.transform.TransformerFactory`,
  `transform.JDOMResult`, `transform.JDOMSource`, `xpath.XPathFactory`) removed; the constants themselves and
  their javadoc `{@link}` text are untouched (unresolved links only, no code effect).
- No class, method or field was removed from the main sources.

### Imported classes that still reference JVM-only APIs (re-shaped by later tasks)

- `input.JDOMParseException`: references `org.xml.sax.SAXParseException` for its line/column/publicId/systemId
  accessors. Task 4 re-shapes it without any sax dependency (plain lineNumber/columnNumber/publicId/systemId fields).
- `AttributeType`: `import org.xml.sax.Attributes` (javadoc-only reference, no code use).
- `output.support.AbstractXMLOutputProcessor`: uses `javax.xml.transform.Result.PI_DISABLE_OUTPUT_ESCAPING` /
  `PI_ENABLE_OUTPUT_ESCAPING` (two string constants compared with the PI target); commonization replaces them with literals.
- `Element`: `java.net.URI`/`URISyntaxException` (namespace URI validation); `Element`, `Document`, `Namespace`:
  Java serialization (`ObjectInputStream`, `InvalidObjectException`); `Content`: `java.io.*`.
  All stay as imported in this task.

### Behaviour/name differences

- The string literals `"org.jdom2.xpath.XPathFactory"` and `"org.jdom2.output.LineSeparator"` (system-property names in
  `JDOMConstants`) were rewritten by the blanket package rename to `fi.sykero.kdom.*`; nothing in JSword/`:app` sets them.

### Removed test methods

- `output/TestXMLOutputter#test_ErrorSurrogatePairOutput`: needs `UncheckedJDOMFactory` (excluded) to build an invalid `Text`.
- `TestIllegalAddExceptn`: not removed; `DefaultJDOMFactory` calls (`fac.element(..)` etc.) replaced by the equivalent
  public constructors (`new Element(..)` etc.).
- Not imported at all (excluded-class tests): all of `cases/{input,located,adapters,transform,xpath,jaxb,serialize}`,
  `*JDOMFactory*` tests, `TestBridgeMethods`, and in `cases/output` the
  DOM/SAX/StAX outputter and round-trip tests; `test/util/FidoFetch`; `special/TestIssue008*`; `util/TestReflectionConstructor`.

### Ignored tests (`// K5: needs KdomBuilder (Task 9)` + `@Ignore("needs KdomBuilder, Task 9")`)

Each parse goes through a private placeholder `parseWithKdomBuilder(String)` in the test class (throws
`UnsupportedOperationException`); Task 9 fills the body with `KdomBuilder` and removes the `@Ignore`s.

- `test.cases.output.TestXMLOutputter#test_HighSurrogatePair`
- `test.cases.output.TestXMLOutputter#test_HighSurrogatePairDecimal`
- `test.cases.output.TestXMLOutputter#test_HighSurrogateAttPair`
- `test.cases.output.TestXMLOutputter#test_HighSurrogateAttPairDecimal`
- `test.cases.output.TestXMLOutputter#test_RawSurrogatePair`
- `test.cases.output.TestXMLOutputter#test_RawSurrogatePairUTF8`
- `test.cases.output.TestXMLOutputter#test_ErrorSurrogatePair`
- `test.cases.special.TestIssue173ContentCount#testContentSize`

(2 further skipped tests, `TestProcessingInstruction#testIllegalAttnameC` and `#testIllegalAttValueA`, are `@Ignore`d by jdom2 itself.)

## Baseline

`:kdom:jvmTest` on the JVM after Task 3: 40 test classes, 687 tests, 10 skipped (8 K5 `@Ignore` + 2 upstream), 0 failures,
0 errors. This number may only grow; the 8 K5 ignores return in Task 9.

### Import notes (fix round 1)

- `TestDescendantIterator`, `TestDescendantFilterIterator` and `TestJDOMExceptn` (17 tests) use only imported classes and are
  imported unchanged under `test/cases/`; all run green (none ignored).
- Line endings: the CRLF originals of `UnitTestUtil.java` and `complex.xml` were normalised to LF by git.
- `xmlchars.xml` and `complex.xml` are copied to `src/jvmTest/resources` but are currently unused by the kept tests
  (Task 8 embeds xmlchars).
- Known stale string: `output/LineSeparator.java` javadoc/comment (lines ~109, ~171) still says
  `-Dorg.jdom2.output.LineSeparator`; left as imported, Task 7 removes it together with `SystemProperty`.

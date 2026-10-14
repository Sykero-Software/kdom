# Translation notes

Differences between kdom and jdom2 2.0.6.1. Every intended behavioural or API difference gets a test
and a line here.

## Property rule exceptions

- `Namespace.getURI()` -> property `uri` with `@get:JvmName("getURI")` (a plain `val uri` would compile to `getUri()`
  and break the Java/JVM name). Kotlin callers still write `ns.uri`.
- `util.NamespaceStack.getScope()` -> property `scope` (copying getter, returns a fresh array each read, as in jdom2);
  the former private `scope`/`added` fields became `scopes`/`added` (private) to avoid the name clash.
- `LineSeparator.value()` stays a function: it is not a `getX()` getter in jdom2.
- Not a property: `NamespaceStack.getNamespaceForPrefix(prefix)`, `getFirstNamespaceForURI(uri)`,
  `getAllNamespacesForURI(uri)`, `getRebound(prefix)` take a parameter and stay functions.

## API shape

Task 4 (leaf classes translated to Kotlin, JVM only; sources under `src/jvmMain/kotlin`). All translated files keep the
JDOM licence header; JVM names are unchanged unless listed here.

Property rule applications:
- `NamespaceAware`: `getNamespacesInScope()/getNamespacesIntroduced()/getNamespacesInherited()` -> `val namespacesInScope`,
  `namespacesIntroduced`, `namespacesInherited` (`List<Namespace>`; Java implementers keep `getX()`).
- `Namespace`: `getPrefix()` -> `val prefix`; `getURI()` -> `val uri` (see exceptions).
- `NamespaceStack`: `getScope()` -> `val scope` (see exceptions).
- `JDOMParseException`: `getPartialDocument()/getPublicId()/getSystemId()/getLineNumber()/getColumnNumber()` -> `val`s.

Shape decisions (no behaviour change intended):
- Static members use `companion object` + `@JvmStatic` / `@JvmField` / `const`: `JDOMConstants` (all `const val`, class kept final
  with a private constructor), `Namespace` (`NO_NAMESPACE` / `XML_NAMESPACE` are `@JvmField`; init order namespacemap,
  NO_NAMESPACE, XML_NAMESPACE, XMLNS_NAMESPACE, pre-population block is the Java order), `ArrayCopy`, `SystemProperty`
  (kept a public no-arg-constructible class as in Java), `Verifier`, `AttributeType` (enum companion).
- Every exception keeps `serialVersionUID = 200L` as a `private const val` in a private companion (static field on the outer
  class, as in Java). The package-private constructors of `IllegalAddException/IllegalDataException/IllegalNameException/
  IllegalTargetException` became `internal` (no Java caller outside the package; Kotlin emits them as public in bytecode, so the
  Java callers in `fi.sykero.kdom` still compile). Their string parameters are nullable and concatenate as `"null"` exactly as
  `StringBuilder.append(String)` did.
- Kotlin artefacts that were not in the Java classes: a `Companion` static field/class on classes with a companion; a public
  synthetic `Namespace(String, String, DefaultConstructorMarker)` constructor next to the private one; `IteratorIterable` (declared with
  `MutableIterator<T>` so `remove()` stays available to Kotlin callers, as in java.util.Iterator) carries the `KMappedMarker` marker interface; the enum classes have `getEntries()`.
- `EscapeStrategy` became a `fun interface` (Java lambdas / anonymous classes still work).
- `LineSeparator`: the Java `private static getDefaultLineSeparator()` is a top-level private function (not a companion
  member) because enum entries call it during enum initialisation, when a companion object does not exist yet.
- `AttributeType.byIndex` is `@Deprecated("Use normal Enums instead of int's")` (Kotlin requires a message); the
  `org.xml.sax.Attributes` import (javadoc-only) was dropped. `typeName.trim()` kept Java semantics (`trim { it <= ' ' }`).
- `ArrayCopy.copyOf/copyOfRange` keep reflection (`Array.newInstance(source.javaClass.componentType, len)`) so the runtime
  component type is preserved; overloads for `CharArray`, `IntArray`, `BooleanArray` are separate functions.
- `NamespaceStack.push(Namespace...)` is `vararg namespaces: Namespace`; the Java `namespaces == null` check is gone (Kotlin
  rejects a null array at the call), the empty case still duplicates the current level. No Java caller passes null.
  The nested `ForwardWalker`/`BackwardWalker`/`EmptyIterable` implement `Iterator<Namespace>` with a plain `fun remove()` that
  throws `UnsupportedOperationException` (same JVM method as the Java override).
- `Verifier`: the two compressed tables are `byteArrayOf` / `intArrayOf` (622 entries each, copied by script); mask tests are
  `0 != (CHARFLAGS[c.code].toInt() and MASK.toInt())` (Java: `(byte)0 != (byte)(flags & mask)`, all masks are < 128).
  `for` loops that mutate the index (`checkCharacterData`) are `while` loops. `String.format` keeps the default locale.
  `checkCDATASection/checkProcessingInstructionData/checkCommentData/checkProcessingInstructionTarget` use `data!!`/`target!!`
  after the null-checking helper returned null (Java NPE-free by construction).
- `jswordAssert`, `splitLikeJava`, `latin1String` were not needed in these 18 files; there is no `assert` in them.

JDOMParseException re-shape (ruling R3): no dependency on `org.xml.sax` or `fi.sykero.kdom.sax`. jdom2 read publicId, systemId,
line and column from a `SAXParseException` cause (`-1` / `null` when the cause was something else). kdom stores them as plain
constructor fields. Constructors: primary `(message, cause, partialDocument, publicId, systemId, lineNumber, columnNumber)`;
secondary `(message)`, `(message, cause)`, `(message, cause, partialDocument)` (jdom2's) and
`(message, cause, lineNumber, columnNumber)`. Defaults are `partialDocument = null`, `publicId = systemId = null`,
`lineNumber = columnNumber = -1`, so the jdom2 constructors give the values jdom2 gave for a non-SAX cause. A parser that
wraps a position-carrying exception must now pass the position explicitly (Task 9). No Java test used these accessors.

## Behaviour differences

- `JDOMParseException` (R3): a SAX-carrying cause no longer supplies line/column/publicId/systemId; callers pass them
  explicitly. The jdom2-style constructors always give -1 / -1 / null / null. Pinned by `JDOMParseExceptionTest`.

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

`:kdom:jvmTest` on the JVM after Task 3: 40 test classes, 704 tests, 10 skipped (8 K5 `@Ignore` + 2 upstream), 0 failures,
0 errors. After Task 4 fix round 1: 41 test classes, 708 tests (+4 `JDOMParseExceptionTest`), 10 skipped, 0 failures. This number may only grow; the 8 K5 ignores return in Task 9.

### Import notes (fix round 1)

- `TestDescendantIterator`, `TestDescendantFilterIterator` and `TestJDOMExceptn` (17 tests) use only imported classes and are
  imported unchanged under `test/cases/`; all run green (none ignored).
- Line endings: the CRLF originals of `UnitTestUtil.java` and `complex.xml` were normalised to LF by git.
- `xmlchars.xml` and `complex.xml` are copied to `src/jvmTest/resources` but are currently unused by the kept tests
  (Task 8 embeds xmlchars).
- Known stale string: `output/LineSeparator.java` javadoc/comment (lines ~109, ~171) still says
  `-Dorg.jdom2.output.LineSeparator`; left as imported, Task 7 removes it together with `SystemProperty`.

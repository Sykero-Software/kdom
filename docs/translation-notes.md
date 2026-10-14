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

- Task 5 (tree core): **fluent setters stay functions.** jdom2's `setX(..)` that returns the receiver (`Text.setText`,
  `Comment.setText`, `EntityRef.setName/setPublicID/setSystemID`, `DocType.setElementName/setPublicID/setSystemID`,
  `ProcessingInstruction.setTarget/setData(String|Map)/setPseudoAttribute`, `Attribute.setName/setNamespace/setValue/
  setAttributeType`, `Element.setName/setNamespace/setText/setAttributes/setContent(..)`, `Document.setRootElement/
  setDocType/setContent(..)`) are kept as `open fun setX(v: T?): Self` next to a `val x` getter (no setter, so no JVM
  clash). Reason: jdom2's own tests (the contract) assert the returned identity (`assertTrue(er == er.setName("n"))`,
  `doc == doc.setDocType(null)`, `new Element("a").setText("1")` chains) and pass `null` where the Java code validates or
  maps it (`Text.setText(null)` -> empty, `setDocType(null)` removes, `Verifier.check*(null)` -> `Illegal*Exception`); a Kotlin
  property setter returns `Unit` and its type would have to equal the getter's (non-null). Kotlin callers write `x.setText(s)`,
  not `x.text = s`. Void jdom2 setters with a getter (`DocType.setInternalSubset`, `Attribute.setSpecified`,
  `Document.setBaseURI`) are real `var` properties (`internalSubset`, `isSpecified`, `baseURI`).
- `Element.getAttributes()` stays a function: it lazily creates the attribute list (javadoc: "may be inefficient if there are no
  Attributes"); `NamespaceStack` and `Verifier` (Task 4 files) now call `element.getAttributes()`.
- `Element.getXMLBaseURI()` stays a function: it walks the ancestry, resolves URIs and throws the checked
  `URISyntaxException` (`@Throws`). (A property would also have the JVM name `getXmlBaseURI`.)
- `Attribute.getIntValue/getLongValue/getFloatValue/getDoubleValue/getBooleanValue` are properties with
  `@get:Throws(DataConversionException::class)` (the Java tests catch the checked exception).
- `ProcessingInstruction.getPseudoAttributeNames()` -> `val pseudoAttributeNames` (copying getter, as `NamespaceStack.scope` in Task 4).
- `ContentFilter.get/setFilterMask` -> `var filterMask` with a custom setter (`setDefaultMask(); mask &= newMask`); the private field is
  `mask` (jdom2: `filterMask`), see Traps.
- Not a property: `Element.getNamespace(prefix)`, `getAttribute*(..)`, `getChild*(..)`, `getChildren(name[, ns])`, `getContent(index|filter)`,
  `getDescendants(filter)`, `Document.getProperty(id)`, `ProcessingInstruction.getPseudoAttributeValue(name)`, `Attribute`/`Element`
  `get/removeAttribute*`, `AttributeList.get(name, ns)` take parameters.

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

Task 5 (tree core and filters translated to Kotlin, JVM only; 28 classes: `CloneBase`, `Content`, `Parent`, `Text`, `CDATA`, `Comment`,
`ProcessingInstruction`, `EntityRef`, `DocType`, `Attribute`, `AttributeList`, `ContentList`, `DescendantIterator`, `FilterIterator`,
`Element`, `Document`, `filter/*` x12). A javap diff of the public/protected JVM API against the Java originals shows only the
differences listed here.

Property rule applications: `Content`: `cType`, `parent` (open; `Text`/`EntityRef` narrow it to `Element?`, `DocType` to
`Document?`), `parentElement`, `document`, `value` (abstract, `String?`); `Parent`: `contentSize`, `content`, `descendants`,
`parent`, `document`; `Element`: `name`, `namespace`, `namespacePrefix`, `namespaceURI`, `qualifiedName`, `additionalNamespaces`,
`value`, `isRootElement`, `text`, `textTrim`, `textNormalize`, `attributesSize`, `children` (live `MutableList<Element>` filtered view),
`content`, `descendants`, `contentSize`; `Document`: `rootElement` (throws `IllegalStateException` as jdom2), `docType`, `baseURI`, `content`,
`parent`, `document`; `Text`: `text`, `textTrim`, `textNormalize`, `value`; `Comment.text`; `EntityRef.name/publicID/systemID`;
`DocType.elementName/publicID/systemID/internalSubset`; `ProcessingInstruction.target/data`; `Attribute.name/namespace/value/
attributeType/isSpecified/parent/document/qualifiedName/namespacePrefix/namespaceURI`; `NegateFilter.baseFilter`. Exceptions: see
"Property rule exceptions".

Shape decisions (no behaviour change intended):
- Nullability follows the Java tests, not just the javadoc: a raw instance made by a protected no-args constructor has `null`
  text/name (`TestText`, `TestComment`, `TestEntityRef`, `TestDocType`, `TestProcessingInstruction`, `TestAttribute`, `TestElement`
  assert it), so `Text.value/text`, `Comment.text`, `EntityRef.name/publicID/systemID`, `DocType.elementName/publicID/systemID`,
  `ProcessingInstruction.target/data/value`, `Attribute.name/value/qualifiedName`, `Element.name/qualifiedName` are `String?`.
  `Attribute.namespace` and `Element.namespace` are `lateinit` (no test sees the raw state; reading it on a raw instance throws
  `UninitializedPropertyAccessException` instead of returning null / NPE). Parameters of every setter/constructor are nullable
  where jdom2 handed `null` to a null-tolerant check (`Verifier.check*`), mirroring the K1 conservative-nullability rule.
- The protected fields of the content classes became properties with a private setter (`Text.value` has a protected setter, written by
  `CDATA`): `name`, `text`, `publicID`, `systemID`, ... keep their JVM field names (serialization by default `defaultWriteObject`
  is unchanged) but are no longer visible to Java subclasses (none exist in kdom). Renamed fields: `Content.parent` -> `parentRef`
  (`@JvmField @Transient internal`: `ContentList`/`AttributeList` set it, as package access did in Java), `Attribute.parent` -> `parentRef`
  likewise, `Element.content/attributes/additionalNamespaces` -> private `contentList`/`attributes`/`additionalNamespaceList`,
  `Document.content` -> `contentList`, `ContentFilter.filterMask` -> `mask`. `Content.ctype` is still the protected final field
  (`@JvmField`), with `cType` the property.
- `Content.setParent`, `Attribute.setParent` are public (were protected = package access in Java): `ContentList`, `AttributeList` and
  `Element` call them on `Content`/`Attribute` receivers, which Kotlin's `protected` does not allow. `CloneBase`, `DescendantIterator`,
  `FilterIterator`, `AndFilter`, `OrFilter`, `NegateFilter`, `PassThroughFilter`, `TextOnlyFilter`, `ClassFilter` (package-private
  in Java): `CloneBase` is public (a public class cannot have an internal supertype), the others and `AttributeList`/`ContentList` are
  `internal` classes with public members (no name mangling; the JVM classes are public).
- `clone()` overrides are explicitly `public override fun clone()` in all tree classes (Kotlin keeps `protected` from `Cloneable.clone`
  otherwise); `CloneBase.clone()` is public (was protected). Cloning is still `Object.clone()` via `super<Cloneable>.clone()`
  (`Attribute` has two supertypes with it: `super<CloneBase>.clone()`). `Serializable`, `readObject/writeObject`, `transient`,
  `serialVersionUID` (`private const val` in a private companion, a static field of the outer class) are kept (Task 7).
- `AttributeList`/`ContentList` extend `AbstractMutableList` with the Java algorithms and the exact `modCount` /
  `ConcurrentModificationException` checks (`AttributeList` uses the inherited `modCount`; `ContentList` keeps its own
  `sizeModCount`/`dataModiCount`). `remove(int)` is `removeAt`, `size()` is the `size` property. The JDK `List.sort(Comparator)`
  override exists (`override fun sort`) but cannot be called from Kotlin (the `MutableList.sort` extension wins), so `sortContent` /
  `sortAttributes` carry the body and `Element` calls them. `FilterList`/`FilterListIterator` are `inner` classes of `ContentList`.
- `Parent.clone(): Any` is `public override fun clone()` in the interface (widens `Cloneable.clone`). `Parent.getContent()` etc. return
  `MutableList` (live views), `removeContent(int)` returns `Content?`.
- `Filter<T>`: `filter(List<*>?)` / `filter(Any?): T?` overloads, `negate(): Filter<*>`, `or(..): Filter<*>` (`AbstractFilter` narrows to
  `Filter<out Content>`). `Filters` is a class with a private constructor and a companion with `@JvmStatic` functions (same static
  JVM methods); `Filters.fboolean/fdouble` use `java.lang.Boolean::class.java` / `java.lang.Double::class.java` (`Boolean::class.java`
  is the primitive class). `ContentFilter` mask constants are `const val` in a companion (JVM static fields).
- Kotlin artefacts not in the Java classes: `Companion` fields/classes; `Text.EMPTY_STRING` is `internal const` (a public static
  field); `Text` has a protected `setValue(String)` (the protected setter of `value`); `Content$CType.getEntries()`;
  `KMutableIterator`/`KMutableListIterator` markers on the iterators; synthetic `access$` methods on the list classes.
- The `Element(String, Namespace)`/`Element(String, String)` constructors etc. take nullable parameters; `Element(name)` delegates to
  `this(name, null as Namespace?)` to pick the overload.

## Behaviour differences

- `JDOMParseException` (R3): a SAX-carrying cause no longer supplies line/column/publicId/systemId; callers pass them
  explicitly. The jdom2-style constructors always give -1 / -1 / null / null. Pinned by `JDOMParseExceptionTest`.
- Task 5: `Attribute.getNamespace()`/`Element.getNamespace()` on a raw (protected no-args) instance throw
  `UninitializedPropertyAccessException` instead of returning null (untested; see API shape).

## Task 5 traps (met while translating)

- **Kotlin `protected` is stricter than Java `protected`.** Java `protected` is also package access, which `ContentList`,
  `AttributeList`, `Element`, `Document` relied on (`child.setParent(parent)`, `c.parent = parent`, `a.clone()` on `Content`
  receivers). Kotlin allows protected access only on a receiver of the subclass type, so these members became public (`setParent`,
  `clone()` overrides) or `@JvmField internal` (`parentRef`).
- **A property named like a field.** `Content.parent`, `Element.content/attributes/additionalNamespaces`, `Document.content` were fields
  *and* getter names; the property rule needs the name, so the fields were renamed (above). Keep the JVM field names of the
  serialized fields (`name`, `namespace`, `value`, `type`, `specified`, `ctype`, `elementName`, ...) so default serialization is
  unchanged: that is why `Attribute.type/specified` are private fields with `attributeType`/`isSpecified` accessors.
- **`size` inside an inner class.** In `ContentList`, `FilterList` (an inner class that is itself a list) and the iterators referred to the
  outer field `size`. In Kotlin the `FilterList.size` property would shadow it, so the outer field is `count`.
- **`AbstractMutableList.modCount`** exists on the JVM (inherited from `java.util.AbstractList`) and is accessible from the subclass and
  its inner classes. It must be re-declared on Native in Task 7 (spec M0.4); `ContentList` does not use it (own counters).
- **`sort` override is invisible to Kotlin callers**, see API shape; calling `contentList.sort(cmp)` resolves to the stdlib extension.
- **Overloaded `filter(List)` vs `filter(Object)`** are chosen by static type, as in Java; call sites that hold `Any?` pick the right one
  (`AbstractFilter.matches`, `ContentList.FilterList.resync`, `FilterIterator`).
- **`Boolean::class.java` is the primitive `boolean` class** (`isInstance` is always false): `Filters.fboolean()` must wrap
  `java.lang.Boolean::class.java`.
- **Identity comparisons**: `Text.value === EMPTY_STRING` (`CDATA.append`), `ns === Namespace.NO_NAMESPACE`, `tfirst.parent !== text.parent`
  (`Element.coalesceText`), `(left + right) ushr 1`, `~x` -> `.inv()`; `String.trim()` -> `trim { it <= ' ' }` (`Element.getTextTrim`,
  `Attribute.get*Value`, `ProcessingInstruction.parseData`).
- **`Map.get/remove(null)`**: `ProcessingInstruction.getPseudoAttributeValue(null)` / `removePseudoAttribute(null)` pass a nullable key to a
  `LinkedHashMap`; they cast to `Map<String?, String>` (unchecked) to keep the Java call.
- **Static init order.** `Filters` keeps the Java field order in its companion; `Attribute` keeps the `*_TYPE` constants in the companion.

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
0 errors. After Task 4 fix round 1: 41 test classes, 708 tests (+4 `JDOMParseExceptionTest`), 10 skipped, 0 failures. After Task 5 (tree core and filters in Kotlin): unchanged, 41 classes / 708 tests / 10 skipped / 0 failures. This number may only grow; the 8 K5 ignores return in Task 9.

### Import notes (fix round 1)

- `TestDescendantIterator`, `TestDescendantFilterIterator` and `TestJDOMExceptn` (17 tests) use only imported classes and are
  imported unchanged under `test/cases/`; all run green (none ignored).
- Line endings: the CRLF originals of `UnitTestUtil.java` and `complex.xml` were normalised to LF by git.
- `xmlchars.xml` and `complex.xml` are copied to `src/jvmTest/resources` but are currently unused by the kept tests
  (Task 8 embeds xmlchars).
- Known stale string: `output/LineSeparator.java` javadoc/comment (lines ~109, ~171) still says
  `-Dorg.jdom2.output.LineSeparator`; left as imported, Task 7 removes it together with `SystemProperty`.

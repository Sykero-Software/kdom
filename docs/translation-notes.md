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

- Task 5 (tree core), **dual setter API (ruling R8)**: every jdom2 fluent setter with a matching no-arg getter is kept as the jdom2-shaped
  `open fun setX(v: T?): Self` (returns the receiver, accepts `null` as jdom2 did: jdom2's own tests assert `er == er.setName("n")`,
  `doc == doc.setDocType(null)`, chains like `new Element("a").setText("1")`) **and** the getter is a mutable property `var x: T` whose
  setter is `@JvmSynthetic set(v) { setX(v) }`, so Kotlin callers keep jdom2's synthetic-property assignment (`e.name = "a"`,
  `t.text = "b"`, as used in `app/.../EpubOptimization.kt`, `ClientPageObjects.kt`). `@JvmSynthetic` hides the property setter from
  javac, the JVM only has the fluent `setX(..)`; no platform clash arose (descriptors differ by return type, and the property setter
  is synthetic). Covered: `Text.text`, `Comment.text`, `EntityRef.name/publicID/systemID`, `DocType.elementName/publicID/systemID`,
  `ProcessingInstruction.target/data`, `Attribute.name/namespace/value/attributeType`, `Element.name/namespace/text/attributes/content`,
  `Document.rootElement/docType/content`. Proven by `PropertySyntaxTest` (Kotlin assignment, chains, identity). The setter parameter
  type is the non-null property type; call `setX(null)` for the jdom2 null handling. Void jdom2 setters (`DocType.setInternalSubset`,
  `Attribute.setSpecified`, `Document.setBaseURI`) are plain `var` properties. Task 7: `kotlin.jvm.JvmSynthetic`,
  `JvmField`, `JvmStatic`, `JvmName`, `JvmOverloads` and `Throws` compile in `commonMain` for jvm, iosArm64 and iosSimulatorArm64
  (probe compiled 2026-10-14; the moved sources import them from `kotlin.jvm`), so the dual setter API is unchanged in common code.
- `Element.getXMLBaseURI()` was kept as a function in Task 5; Task 7 removed it (`java.net.URI`, see Task 7 API changes).
- `Attribute.getIntValue/getLongValue/getFloatValue/getDoubleValue/getBooleanValue` are properties with
  `@get:GetterThrows(DataConversionException::class)` (the Java tests catch the checked exception). `GetterThrows` (Task 7) is an
  `@OptionalExpectation` annotation in `fi.sykero.kdom.internal` whose JVM actual is `kotlin.jvm.Throws`: common `kotlin.Throws`
  cannot target a getter.
- `ProcessingInstruction.getPseudoAttributeNames()` -> `val pseudoAttributeNames` (copying getter, as `NamespaceStack.scope` in Task 4).
- `ContentFilter.get/setFilterMask` -> `var filterMask` (backing field `filterMask`, as jdom2) whose setter applies the default mask
  (`setDefaultMask(); filterMask &= mask` collapses to `field = DEFAULT_MASK and mask`; every mask the class assigns is a subset).
- Task 6 output API shape (see "Task 6" under API shape): `XMLOutputter.xmlOutputProcessor` keeps the JVM names
  `getXMLOutputProcessor`/`setXMLOutputProcessor` through `@get:JvmName`/`@set:JvmName`, and
  `Format.getRawFormat/getPrettyFormat/getCompactFormat` stay `@JvmStatic` companion factory functions (not properties).
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

Property rule applications (all `var` ones also have the fluent `setX`, see exceptions): `Content`: `cType`, `parent` (open; `Text`/`EntityRef` narrow it to `Element?`, `DocType` to
`Document?`), `parentElement`, `document`, `value` (abstract, `String?`); `Parent`: `contentSize`, `content`, `descendants`,
`parent`, `document`; `Element`: `name`, `namespace`, `namespacePrefix`, `namespaceURI`, `qualifiedName`, `additionalNamespaces`,
`value`, `isRootElement`, `text`, `textTrim`, `textNormalize`, `attributesSize`, `children` (live `MutableList<Element>` filtered view),
`content`, `descendants`, `contentSize`; `Document`: `rootElement` (throws `IllegalStateException` as jdom2), `docType`, `baseURI`, `content`,
`parent`, `document`; `Text`: `text`, `textTrim`, `textNormalize`, `value`; `Comment.text`; `EntityRef.name/publicID/systemID`;
`DocType.elementName/publicID/systemID/internalSubset`; `ProcessingInstruction.target/data`; `Attribute.name/namespace/value/
attributeType/isSpecified/parent/document/qualifiedName/namespacePrefix/namespaceURI`; `NegateFilter.baseFilter`. Exceptions: see
"Property rule exceptions".

Shape decisions (no behaviour change intended):
- **Raw instances.** A raw instance (a subclass created through a protected no-args constructor) returns `null` from the non-null
  typed properties; production code never creates them. jdom2's tests pin this (`TestText`, `TestComment`, `TestEntityRef`,
  `TestDocType`, `TestProcessingInstruction`, `TestAttribute`, `TestElement`). The properties are typed non-null (`Element.name`,
  `Element.namespace`, `qualifiedName`, `Text.text/value`, `Comment.text`, `EntityRef.name`, `DocType.elementName`,
  `ProcessingInstruction.target/data/value`, `Attribute.name/namespace/value/qualifiedName`, `Content.value`) with a private nullable
  backing field and the getter `get() = unsafeNull(field)` (`fi.sykero.kdom.internal.unsafeNull`, an erased generic cast: no null check).
  Genuinely nullable properties stay `T?` (`publicID`, `systemID`, `internalSubset`, `parent`, `docType`, ...). The `!!` that turned
  raw nulls into NPE were removed. Setter/constructor parameters are nullable where jdom2 handed `null` to a null-tolerant check
  (`Verifier.check*`), mirroring the K1 conservative-nullability rule.
- **Open vs final.** Kotlin members are final unless a jdom2 test or kdom needs an override. `open`: all fluent setters (`setX`),
  `detach`, `clone`, `setParent`, `Text.append`, `CDATA`/`Element`/`Document` overrides of those, `Content` abstract members and the
  `Parent`/`NamespaceAware`/`Filter` members (interfaces); everything else (getters, `getChild*`, `getAttribute*`, `remove*` helpers,
  ...) is final although jdom2 allowed overriding it. kdom has no subclassers outside the tests' anonymous raw subclasses.
  The output walkers `WalkerPRESERVE`, `WalkerTRIM`, `WalkerTRIM_FULL_WHITE`, `WalkerNORMALIZE` are final classes too (public
  non-final in jdom2), as are `FormatStack` and `XMLOutputter` (final in jdom2).
- The protected fields of the content classes became properties; the nullable storage is a private field `xField` (`nameField`,
  `textField`, `publicIDField`, ...; `Text.textValue` is `@JvmField protected`, written by `CDATA`). **The serialized field names of
  these classes therefore changed** (default Java serialization of `name`, `value`, ... is no longer jdom2's); no serialized data
  exists in kdom and Task 7 removed serialization. Kept: `Content.ctype` (protected final field, `cType` the property),
  `Attribute.type`/`specified`, `ContentFilter.filterMask`. Renamed: `Content.parent`/`Attribute.parent` -> `parentRef` (`@JvmField
  @Transient internal`: the list classes set it, as package access did in Java), `Element.content/attributes/additionalNamespaces`
  -> private `contentList`/`attributeListField`/`additionalNamespaceList`, `Document.content` -> `contentList`.
- `Content.setParent`, `Attribute.setParent` are `internal` (they were package access in Java; no Java caller remains, so the JVM
  name mangling does not matter). `CloneBase` is public (a public class cannot have an internal supertype); `DescendantIterator`,
  `FilterIterator`, `AndFilter`, `OrFilter`, `NegateFilter`, `PassThroughFilter`, `TextOnlyFilter`, `ClassFilter`, `AttributeList`,
  `ContentList` (package-private in Java) are `internal` classes with public members (no mangling; the JVM classes are public).
- `clone()` overrides are explicitly `public override fun clone()` in all tree classes (Kotlin keeps `protected` from `Cloneable.clone`
  otherwise); `CloneBase.clone()` is public (was protected). Cloning is still `Object.clone()` via `super<Cloneable>.clone()`
  (`Attribute` has two supertypes with it: `super<CloneBase>.clone()`). `Serializable`, `readObject/writeObject`, `transient`,
  `serialVersionUID` (`private const val` in a private companion, a static field of the outer class) were kept until Task 7, which
  replaced `clone()` and removed serialization (see Task 7).
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
  field); `Content$CType.getEntries()`;
  `KMutableIterator`/`KMutableListIterator` markers on the iterators; synthetic `access$` methods on the list classes.
- The `Element(String, Namespace)`/`Element(String, String)` constructors etc. take nullable parameters; `Element(name)` delegates to
  `this(name, null as Namespace?)` to pick the overload.

Task 6 (output package translated to Kotlin, JVM only; `Format`, `XMLOutputter`, `output/support/*`: 12 classes; `src/jvmMain/java` is gone).
`java.io.Writer`/`OutputStream`/`java.nio.charset.CharsetEncoder`/`javax.xml.transform.Result` stay (Task 7 commonizes them).

Property rule applications: `Format`: `escapeStrategy`, `lineSeparator` (`String?`), `omitEncoding`, `omitDeclaration`, `expandEmptyElements`,
`ignoreTrAXEscapingPIs` (void setter: plain `var`), `textMode`, `indent` (`String?`), `encoding`, `isSpecifiedAttributesOnly` (void setter);
`FormatStack`: `indent`, `lineSeparator`, `encoding`, `isOmitDeclaration`, `isOmitEncoding`, `isExpandEmptyElements`, `isSpecifiedAttributesOnly`,
`escapeStrategy`, `defaultMode`, `padBetween`, `padLast` (vals) and `isIgnoreTrAXEscapingPIs`, `escapeOutput`, `levelIndent`, `levelEOL`, `textMode`
(vars; setters are plain void, as in jdom2; the per-depth arrays are private `*s`/`*Stack` fields to avoid the name clash); `Walker`:
`isAllText`, `isAllWhitespace`, `isCDATA` (`hasNext()`, `next()`, `text()` stay functions); `XMLOutputter`: `format`, `xmlOutputProcessor`.
Exceptions: `XMLOutputter.getXMLOutputProcessor/setXMLOutputProcessor` -> `var xmlOutputProcessor` with `@get:JvmName("getXMLOutputProcessor")` /
`@set:JvmName("setXMLOutputProcessor")` (a plain property would compile to `getXmlOutputProcessor`; Kotlin callers write `xmlOutputProcessor`
exactly as before). `Format.getRawFormat/getPrettyFormat/getCompactFormat` are `@JvmStatic` companion functions, not properties (factories).

Shape decisions:
- **Dual setter API on `Format`** as in Task 5: `open fun setX(v): Format` (the fluent setters `setEscapeStrategy`, `setLineSeparator(String?)`,
  `setLineSeparator(LineSeparator?)`, `setOmitEncoding`, `setOmitDeclaration`, `setExpandEmptyElements`, `setTextMode`, `setIndent`, `setEncoding`) next to
  `var x` with a `@JvmSynthetic` setter. `Format` has a private constructor, so it cannot be subclassed; the setter bodies live in the property setters
  (`encoding` also re-chooses the `EscapeStrategy`, `lineSeparator` maps `""` to `null`) and the fluent functions delegate to them. Parameter nullability:
  `setIndent`/`setLineSeparator` accept `null` (meaningful), `setEncoding/setTextMode/setEscapeStrategy` take non-null (jdom2 threw an NPE later at output,
  or immediately for the encoding; no AndBible caller passes null).
- `Format.TextMode` is a nested `enum class`; the private escape strategies are nested private classes, the shared instances are private companion vals
  (Java declaration order kept).
- **Protected hooks.** Every `protected` member of `AbstractXMLOutputProcessor` (`write`, `textRaw` x2, `textEntityRef`, `textCDATA`, `attributeEscapedEntitiesFilter`,
  `printDocument/Declaration/DocType/ProcessingInstruction/Comment/EntityRef/CDATA/Text/Element/Content/Namespace/Attribute`) is `protected open` with jdom2's
  exact parameter list; the `process(...)` implementations are `open` too (jdom2 allowed overriding all). `CDATAPRE/CDATAPOST` are `protected const val` in the
  companion (static protected fields on the outer class). `AbstractOutputProcessor.buildWalker` is `protected open`. The `Writer`/`FormatStack`/`Text`... parameters are
  non-null (a Kotlin subclass in :app overrides `printText(out: Writer, fstack: FormatStack, text: Text)`); `write`, `textRaw(String)`, `textEntityRef`, `textCDATA`,
  `attributeEscapedEntitiesFilter` take `String?` because jdom2's `write` ignores null and `walker.text()` is nullable.
- `AbstractFormattedWalker`: `protected abstract analyzeMultiText`, `protected fun get(index)`; `next/hasNext/text/isAllText/isAllWhitespace/isCDATA` are final
  (as in jdom2). `Trim` is a `protected enum class`, `MultiText` a `protected inner class` whose constructor is `internal` (the outer class constructs it; a private
  inner constructor is not reachable from the outer class in Kotlin) and whose `forceAppend` is `internal` for the same reason (mangled JVM name; it was private).
  The Walker subclasses (`WalkerTRIM`, ...) and `Walker` implementations are plain classes with the jdom2 constructors.
- `FormatStack` is final (as in jdom2); `XMLOutputter` is final. `XMLOutputter(Format?, XMLOutputProcessor?)` is the primary constructor, the three other jdom2
  constructors are secondary ones.
- `XMLOutputter.toString()` read `Format`'s package-private fields in jdom2; it reads the properties now. `lineSeparator!!` keeps jdom2's NPE for a null separator
  (jdom2 2.0.6.1 throws from `toString()` for `LineSeparator.NONE`, covered by a comment in `OutputDifferentialTest`).
- `Walker.next()` returns `Content?` (null means "text follows"), `Walker.text()` `String?`.

Output sanity check: `src/jvmTest/kotlin/fi/sykero/kdom/diff/OutputDifferentialTest.kt` (4 tests) builds the same trees in jdom2 and kdom and compares raw/pretty/compact
(and five further format variants: encodings US-ASCII/ISO-8859-1/UTF-16, line separators, indent, expand-empty, omit declaration/encoding, TRIM_FULL_WHITE, NORMALIZE),
string/writer/stream entry points, escaping helpers, TrAX PIs and `toString()` character for character. The full outputter differential is Task 12.

Task 7 (commonization: everything except `internal/Platform.jvm.kt` and `output/XMLOutputterJvm.kt` moved to `src/commonMain/kotlin`;
common code uses only the Kotlin stdlib, no `java.*`/`javax.*`). API changes:
- **Output to `Appendable`.** `XMLOutputter.output(x, out: Appendable)` / `outputElementContent(e, out: Appendable)` replace the `Writer`
  overloads (a `java.io.Writer` is an `Appendable`, so `output(doc, writer)` still compiles) and end with `flushIfPossible(out)`
  (`internal expect fun`; JVM: `(out as? java.io.Flushable)?.flush()`, iOS: no-op). `XMLOutputProcessor.process(out: Appendable, ..)` and every
  `AbstractXMLOutputProcessor` `write`/`text*`/`print*`/`attributeEscapedEntitiesFilter` hook take `Appendable` (`out.write(str)` ->
  `out.append(str)`). `outputString`/`escapeAttributeEntities` write to a `StringBuilder`. The `@Throws(IOException::class)` clauses stay:
  `fi.sykero.kdom.internal.IOException` is an `expect open class`, `actual typealias` to `java.io.IOException` on the JVM.
- **`OutputStream` overloads are JVM extensions.** `XMLOutputterJvm.kt` declares `fun XMLOutputter.output(x, out: java.io.OutputStream)` for
  `Document`, `DocType`, `Element`, `List<Content>`, `CDATA`, `Text`, `Comment`, `ProcessingInstruction`, `EntityRef` and
  `outputElementContent(Element, OutputStream)`, encoding with `Format.encoding` through jdom2's `BufferedWriter(OutputStreamWriter(
  BufferedOutputStream(out), encoding))`. Kotlin callers import `fi.sykero.kdom.output.output`; Java callers use
  `XMLOutputterJvmKt.output(outputter, doc, stream)` (`TestXMLOutputter` was adapted, including its reflective `checkOutput`).
- **TrAX PI targets**: `javax.xml.transform.Result.PI_DISABLE/ENABLE_OUTPUT_ESCAPING` are private string constants with the JAXP values.
- **`Format` default escape strategy** is chosen from the encoding name only: `UTF-8`/`UTF-16` (jdom2), then `ISO-8859-*`/`Latin1`
  (jdom2), `US-ASCII`/`ASCII` (jdom2), then (replacing jdom2's `Charset.forName(enc).newEncoder().canEncode(ch)`) `UTF8`, `UTF-16*`,
  `UTF-32*` -> escape only high surrogates (as the UTF strategy), `ISO8859_1` -> escape above 0xFF, anything else -> escape above 0x7F.
  See Behaviour differences.
- **`SystemProperty` removed.** `LineSeparator.DEFAULT` is jdom2's hard default `"\r\n"`; jdom2's system-property override
  (`JDOMConstants.JDOM2_PROPERTY_LINE_SEPARATOR`, constant kept) is gone. `LineSeparator.SYSTEM` is `systemLineSeparator()` (JVM:
  `System.getProperty("line.separator")`, iOS: `"\n"`). Pinned by `LineSeparatorTest`.
- **`Element.getXMLBaseURI()` removed** (it returned `java.net.URI`; `grep -rn XMLBaseURI jsword/src app/src` finds no caller).
- **`clone()`.** No `Cloneable`/`Object.clone()`. `CloneBase` is an `abstract class` with `protected abstract fun shallowCopy(): CloneBase`
  (a new instance with every field copied by reference, what `Object.clone()` did) and `open fun clone(): CloneBase = shallowCopy()`.
  `Text`, `CDATA`, `Comment`, `ProcessingInstruction`, `EntityRef`, `DocType`, `Attribute`, `Element`, `Document` implement `shallowCopy`
  through their protected/public no-arg constructors; their `clone()` overrides keep jdom2's deep-copy code unchanged (covariant
  returns stay). `Parent.clone(): Any` is a plain interface function. `Format.clone()` copies its ten fields explicitly (encoding
  before the escape strategy, so the copied strategy wins); `XMLOutputter.clone()` shares the `Format` and processor instances, as
  jdom2's shallow `Object.clone()` did. Pinned by jdom2's clone tests and `CloneTest`.
- **Serialization removed.** `Serializable` (on `Content`, `Attribute`, `Parent`, `Namespace`, `Filter`), `readObject`/`writeObject`
  (`Element`, `Document`), `Namespace`'s serialization proxy (`writeReplace`/`readResolve`), `@Transient` and every `serialVersionUID`
  (and the private companions that only held it). The tests that only tested serialization are removed (below).
- **`Namespace` cache**: an immutable snapshot `Map<String, Map<String, Namespace>>` in a `kotlin.concurrent.atomics.AtomicReference`
  (`@OptIn(ExperimentalAtomicApi::class)`), replaced copy-on-write by a CAS loop (`register`); a lookup reads one snapshot without
  locking. The returned instances stay canonical (`NamespaceCacheConcurrencyTest`, common: JVM threads / iOS workers). jdom2 registered
  an empty prefix map for a newly validated URI before validating the prefix; kdom registers the URI with its first Namespace, which
  only means a valid URI whose first prefix was rejected is validated again next time (same result).
- **Collections.** `Collections.unmodifiableList` -> `internal.UnmodifiableList` (a read-only `AbstractList` view; Java-side mutators
  throw `UnsupportedOperationException` as before), `emptyList()`/`singletonList()` -> `emptyList()`/`listOf()`,
  `Collections.emptyMap()` in `ProcessingInstruction.parseData` -> `internal.EmptyUnmodifiableMap` (stored in the mutable `mapData`
  field: `put` throws `UnsupportedOperationException` as before), `System.arraycopy` -> `copyInto`, `Arrays.sort` -> `sort`/`sortWith`
  (stable, as `Arrays.sort` on objects), `java.util.ArrayList/HashMap/LinkedHashMap/Comparator/ConcurrentModificationException/
  NoSuchElementException/RandomAccess` -> the Kotlin stdlib types. `TreeMap` (two uses: `Element.namespacesInScope`,
  `Attribute.orderFirst`) -> `LinkedHashMap` plus `keys.sorted()` at the read; same order (pinned against jdom2 by
  `CommonCollectionsTest`). `ArrayCopy.copyOf/copyOfRange` use `Array.copyOf` (keeps the runtime component type) instead of
  `java.lang.reflect.Array`.
- **Live lists** (`AttributeList`, `ContentList`, `ContentList.FilterList`) extend `internal expect abstract class KdomMutableList<E> :
  AbstractMutableList<E>`. Its JVM actual keeps jdom2's `List.sort(Comparator)` override (calling the list's in-place sort, also what
  Kotlin's `sortWith` reaches on the JVM); common code has no `List.sort` member, so on iOS `sortWith` on a live list is unsupported (use
  `Element.sortContent/sortChildren/sortAttributes`). `modCount` is visible from common `AbstractMutableList` (compiled for iOS).
- **Strings and chars.** `String.format("%0nx", v)` in `Verifier` messages -> `internal.hex(v, n)` (same text);
  `Integer.toHexString(i)` -> `i.toString(16)` (non-negative values only); `Character.isDigit` -> `Char.isDigit()`;
  `Character.isWhitespace` (`ProcessingInstruction.parseData`) -> `internal.isJavaWhitespace` (Java semantics: no-break spaces are not
  whitespace; pinned for all 65536 chars by `CommonHelpersTest`); `StringBuilder.setLength(0)` -> `clear()`, `setLength(len - 1)` ->
  `deleteAt(len - 1)`; `Float.valueOf`/`Double.valueOf` -> `toFloat()`/`toDouble()` (the same JDK parser on the JVM);
  `catch (StringIndexOutOfBoundsException)` -> `catch (IndexOutOfBoundsException)` (its supertype); `Math.max` -> `maxOf`;
  `Locale.ENGLISH` upper-casing -> `uppercase()` (locale-invariant). `String.intern` and `System.identityHashCode` were not used.
- **Class names.** `obj.javaClass.name` in `ContentList`'s `IllegalAddException` messages -> `internal.className(obj)` (JVM: unchanged
  `getClass().getName()`; iOS: the qualified Kotlin name). `ClassFilter` holds a `KClass` (`KClass.isInstance` matches boxed values for
  `Double::class`/`Boolean::class`, which jdom2 needed `java.lang.Double.class` for); its `toString()` shows `className(kclass)` (JVM:
  the boxed Java name, unchanged). `Filters.fclass(clazz: KClass<F>)` replaces `fclass(Class<F>)` (Java callers pass
  `JvmClassMappingKt.getKotlinClass(X.class)`, as `TestFilters` now does). `ContentFilter` uses `is Content`; `TextOnlyFilter.hashCode()`
  hashes `this::class`.
- **Annotations in common code.** `kotlin.jvm.JvmField/JvmStatic/JvmName/JvmSynthetic/JvmOverloads` and `kotlin.Throws` are imported
  explicitly and compile for iOS (no-ops there). Kotlin/Native requires an override to repeat the overridden member's `@Throws`, so
  `Parent.canContainContent` and `Document.canContainContent` now declare `@Throws(IllegalAddException::class)` like
  `Element.canContainContent` (jdom2 declared it on the interface too).
- **Raw instances on Kotlin/Native** (`internal.unsafeNull`): the erased generic cast is unchecked on Native as on the JVM, so a raw
  instance's non-null-typed property returns `null` there too; Native inserts no check at the generic return. Using that value as
  non-null (a member call) is undefined on Native (no NPE guarantee). Production code never creates raw instances, and a `== null` check
  on a non-null-typed value may be folded away by the compiler, so no common test pins it; the 8 Java raw-instance tests stay JVM-only.

## XmlEventParser and KdomBuilder (Task 9, new code)

New public API (`commonMain`): `fi.sykero.kdom.sax.{SAXException, SAXParseException, Locator, Attributes, ContentHandler}`
(SAX2 shapes; `Attributes.length` is a property), `fi.sykero.kdom.input.XmlEventParser(namespaceAware)` with
`parse(String, ContentHandler)` / `parse(ByteArray, ContentHandler)`, and `fi.sykero.kdom.input.KdomBuilder` with
`build(String)` / `build(ByteArray)`; JVM-only extensions in `KdomBuilderJvm.kt` (`build(Reader)`, `build(InputStream)`,
`build(File)`: read fully, then parse; the stream is not closed; read errors are `IOException`). Internal: `TreeBuildingHandler`
(jdom2 2.0.6.1 `SAXHandler` without the DTD parts), `LexicalEventHandler` (comment / CDATA events; there is no public
`LexicalHandler`), `ParsedAttributes`, `SourceScanner`, `XmlSourceMask`, `XmlByteDecoder`. How the probes' requirements P1..P8
are met: `docs/xmlutil-probes.md`. New files carry no licence header (like the other new kdom files, e.g. `internal/Chars.kt`).

Tree rules (as jdom2's `SAXBuilder`): adjacent character data is one `Text`; CDATA is a `CDATA` node; empty comments are
dropped and an empty CDATA section is kept; jdom2's quirk that text directly before an *empty* CDATA section becomes that
`CDATA` (`y<![CDATA[]]>` gives `CDATA("y")`) is kept; comments/PIs inside and outside the root are kept; white space-only
text is kept; namespace declarations become the element namespace / additional namespaces; attributes keep document order,
type `CDATA`, `specified`; the XML declaration is not in the tree. Errors: `JDOMParseException("Error on line N: msg",
cause = SAXParseException, partialDocument (when the root was started), null, null, line, column)`, as jdom2's
`SAXBuilderEngine` (R3 shape). Pinned by `KdomBuilderTest` (common) and `KdomBuilderDifferentialTest` (jvmTest, against jdom2).

**Tree-layer failures are wrapped** (fix round 1): input the JDK parser accepts but jdom2's tree layer rejects with an
unchecked exception (`<:a/>` -> `IllegalNameException`, `<?p:i d?>` -> `IllegalTargetException`, `xmlns:p=" u"` ->
`IllegalNameException`) makes `KdomBuilder.build` throw `JDOMParseException` (cause = that exception, line/column of the
event, partial document when the root was started). jdom2's `SAXBuilder` let the unchecked exception escape; kdom never does,
so a `catch (JDOMException)` fallback chain (JSword) sees every rejected input. Pinned by `KdomBuilderTest` and
`KdomBuilderDifferentialTest.treeLayerRejectionsAreWrapped`.

`XmlEventParser.parse` declares `@Throws(SAXException::class)` and `KdomBuilder.build` `@Throws(JDOMException::class)`
(Java callers can catch the checked exceptions; compiles for iOS).

Remaining differences from a JAXP SAX parser / jdom2 `SAXBuilder` (all deliberate):
- **DOCTYPE is rejected** (plan rule P7; SAX accepts it and expands internal entities).
- **Errors found by the raw pre-scan are reported before any event** (illegal character, `<` in an attribute value, bad
  character reference, DOCTYPE, raw duplicate attribute, missing white space between attributes, `]]>` in text, unterminated
  comment/CDATA/PI). SAX reports the events before the error first; only a handler that keeps state across a failed parse (or
  `JDOMParseException.partialDocument`, then `null`) can tell.
- **Messages and positions** are kdom's/xmlutil's, not Xerces's. Pre-scan errors give the line/column of the offending
  character (Xerces usually reports the column after it); reader errors give where xmlutil stopped.
- **XML 1.1** documents (`version="1.1"`) are accepted but read with the XML 1.0 character and line-end rules.
- **Byte input** supports UTF-8, UTF-16 (BOM or `<?xml` pattern), ISO-8859-1 and US-ASCII (with common aliases); any other
  declared encoding (e.g. `windows-1252`) and a `UTF-16` declaration without UTF-16 bytes are rejected. As in the JDK parser,
  the declaration wins over a UTF-8 BOM (`EF BB BF` + `encoding="ISO-8859-1"` reads ISO-8859-1), and a UTF-16 BOM (or BOM-less
  UTF-16) with a declaration naming another encoding or the other byte order is rejected. The JDK decodes the non-IANA alias
  `utf8` leniently (malformed bytes become U+FFFD); kdom decodes every UTF-8 alias strictly. AndBible parses strings; byte
  input serves EPUB and other file callers.
- **Encoding name in a string's XML declaration**: the JDK ignores the `encoding` value when it reads characters, even a
  malformed one (`encoding="U&F-8"`); xmlutil reads the declaration and rejects it. Malformed bytes
  (invalid/overlong/truncated UTF-8, encoded surrogates, a byte above 0x7F in US-ASCII, odd UTF-16 length) are a
  `SAXParseException` / `JDOMParseException` with the line of the decoded text, where the JDK parser threw a
  `CharConversionException` (an `IOException`, which jdom2 passed through).
- **Locator**: `publicId`/`systemId` are always `null`; line/column come from xmlutil. `Document.baseURI` stays `null`
  (as `SAXBuilder.build(Reader)` without a system id).
- `characters` delivers each run of character data in one call (SAX chunks arbitrarily; the content is equal);
  `ignorableWhitespace` and `skippedEntity` are never called (no DTD; an undefined entity is an error).
- Name validity follows the JDK parser (pre-scan with the XML 1.0 4th-edition tables and its QName rules, fix round 1);
  entity-reference names inside `&...;` are left to xmlutil (an unknown one is rejected anyway).

## Behaviour differences

- `JDOMParseException` (R3): a SAX-carrying cause no longer supplies line/column/publicId/systemId; callers pass them
  explicitly. The jdom2-style constructors always give -1 / -1 / null / null. Pinned by `JDOMParseExceptionTest`.
- **Escape strategy for encodings outside kdom's name list** (Task 7): jdom2 asked the JDK charset whether it can encode a character
  (and escaped only high surrogates for an unknown charset name); kdom escapes every character above 0x7F for such names
  (`windows-1252`, `Shift_JIS`, an unknown name, ...). The listed names give jdom2's answers. Pinned by `FormatEscapeStrategyTest`;
  `TestFormat#testCharsetEncodingEscapes` / `#testIllegalEncodingEscapes` were changed to expect the kdom answers.
- **No line-separator system property** (Task 7): `-Dorg.jdom2.output.LineSeparator=...` (`fi.sykero.kdom.output.LineSeparator`) is
  ignored; `LineSeparator.DEFAULT` is always `"\r\n"`. Pinned by `LineSeparatorTest`: `defaultIgnoresTheJdom2SystemProperty` sets
  the property (the jdom2 name `org.jdom2.output.LineSeparator` and the renamed `fi.sykero.kdom.output.LineSeparator`) to `"\n"`
  in a try/finally, loads a fresh copy of `LineSeparator` through a class loader of its own (the class of the test JVM is
  already initialised, so only a fresh load would read the property) and asserts that `DEFAULT` stays `"\r\n"`.
- **`output(x, System.out)` binds to the `Appendable` member** (Task 7 review, pinned in Task 8): a `PrintStream` is an
  `OutputStream` and an `Appendable`, and a member always wins over an extension function. `outputter.output(doc, System.out)` (and
  any `PrintStream`) therefore calls the member `output(x, Appendable)`: the stream's own encoding encodes the text and
  `Format.encoding` is ignored for the characters (it only names the encoding in the XML declaration). jdom2 routed every
  `OutputStream` through `OutputStreamWriter(format.encoding)`. Only a value whose static type is `OutputStream` (a raw
  `ByteArrayOutputStream`, or a `PrintStream` held in an `OutputStream` variable) reaches the extension
  `XMLOutputter.output(x, OutputStream)` (`import fi.sykero.kdom.output.output`), which keeps jdom2's behaviour. Pinned by
  `OutputStreamBindingTest` (jvmTest, three tests with a `PrintStream(.., "UTF-8"/"ISO-8859-1")` and a raw
  `ByteArrayOutputStream`).
- **Null passed to a non-null setter** (Task 6 shape, pinned in Task 7): `Format.setTextMode(null)`, `Format.setEscapeStrategy(null)`,
  `XMLOutputter.setFormat(null)` and `setXMLOutputProcessor(null)` throw `NullPointerException` at the call (Kotlin parameter check);
  jdom2 stored the null and failed with a `NullPointerException` later, at output (`setFormat(null)` failed at once in jdom2 too).
  Pinned by `output/NullSetterTest` (Java, against both libraries).
- **Clone of a subclass** (Task 7): `clone()` of an instance of a class outside kdom that extends a kdom class (`class MyElement :
  Element("x")`) returns an instance of the kdom class (`Element`), because `shallowCopy()` cannot create an unknown subclass;
  jdom2's `Object.clone()` kept the runtime class. The deep copy is otherwise identical. Pinned by `CloneTest`.
- **Read-only lists and Kotlin casts** (Task 7): the read-only views (`additionalNamespaces`, `namespacesIn*`, filter results) are Kotlin
  read-only lists; a Kotlin `as MutableList` cast fails with `ClassCastException` on the JVM (jdom2's `Collections.unmodifiableList` cast,
  then threw `UnsupportedOperationException`). Java callers still get `UnsupportedOperationException`.
- **Error messages on iOS** (Task 7): class names in `ContentList` messages and `ClassFilter.toString()` are Kotlin qualified names on
  iOS (unchanged on the JVM). `Attribute.floatValue/doubleValue` parse with Kotlin's `toFloat()/toDouble()`, the JDK parser on the JVM;
  the iOS parser's grammar for unusual inputs (hex floats, `f`/`d` suffixes) is not verified here.
- **`sortWith` on a live list on iOS** (Task 7 review): `AttributeList`, `ContentList` and `ContentList.FilterList` extend
  `KdomMutableList`, whose JVM actual keeps jdom2's `List.sort(Comparator)` override (the in-place sort that Kotlin's `sortWith`
  reaches on the JVM). Common code has no `List.sort` member, so on iOS `MutableList.sortWith` falls back to the stdlib's
  generic implementation, which sorts a copy and writes it back through a list iterator calling `set`. kdom's `set` rejects content
  that already has a parent (`IllegalAddException`), and every element of the list has one, so `sortWith` on a live
  `ContentList`/`AttributeList` fails loudly at the first move on iOS (it works on the JVM). `Element.sortContent`,
  `sortChildren` and `sortAttributes` work on every platform and are the supported way to sort.

## Common tests (Task 8)

The jdom2 tests listed in the plan moved from `src/jvmTest/java` to `src/commonTest/kotlin` (same package `fi.sykero.kdom.test...`,
same class and method names, assertion messages kept; `try { ..; fail() } catch (X e) { }` became `assertFailsWith<X>("msg") { .. }`,
the catch-all `catch (Exception e) { fail(..) }` that followed it is covered by `assertFailsWith`). The conversion was mechanical
(a token-based Java-to-Kotlin script) followed by compiler-driven fixes; a translated test replaced its Java original one to one.

Translated (Java `@Test` methods -> Kotlin, base classes count their own methods; subclasses inherit them):
`TestText` 10 -> 9, `TestCDATA` 11 -> 11, `TestComment` 10 -> 9, `TestProcessingInstruction` 30 -> 29, `TestNamespace` 15 -> 15,
`TestAttribute` 31 -> 30, `AbstractTestList` 19 -> 19 (inherited by `TestAttributeList` 5 -> 5 and `TestContentList` 1 -> 1), `TestDocument` 28 -> 28,
`TestElement` 110 -> 109, `TestVerifier` 16 -> 16, `AbstractTestFilter` 0 -> 0 (`TestFilters` 22 -> 22, `TestElementFilter` 5 -> 5, plus
`TestContentFilter` 11 -> 11 and `TestAtributeFilter` 5 -> 5, translated with them because they extend the base), `TestFormat` 22 -> 22,
`AbstractTestOutputter` 63 -> 63 (`TestXMLOutputter` 20 -> 20, of which 7 `@Ignore`d). The five missing methods are the raw-instance tests
(below). `UnitTestUtil` is a common `object` (`@JvmStatic` members), plus top-level `javaAsList` and `nullT`.

Stay in `jvmTest` (Java), with the reason:
- `RawInstanceTest` (new, 5 tests cut unchanged from `TestText#testText`, `TestComment#test_TCC`, `TestProcessingInstruction#testProcessingInstruction`,
  `TestAttribute#test_TCC`, `TestElement#test_TCC`): a raw instance returns `null` from properties typed non-null (see "Raw instances"); the
  `== null` on a non-null type is undefined on Native. `TestEntityRef` and `TestDocType` keep theirs. `TestCDATA#test_TCC` has no null
  assertion and is common.
- `TestPassThroughFilter`: it calls `add` on the filter result, which is a Kotlin read-only list; the Java test expects the JVM's
  `UnsupportedOperationException` from it (Kotlin `as MutableList` would give `ClassCastException`, see "Read-only lists and Kotlin casts").
- `JvmTestUtil` (new): the `Class` flavoured `failNoException`/`checkException`/`failException` used by the Java tests that were not
  translated (`TestEntityRef`, `TestDescendantIterator`, `TestArrayCopy`, `TestPassThroughFilter`, `TestFilterList`), and `deSerialize`.
  The jdom2 "Android" switch (`setAndroid`) had no caller and was dropped.
- Not in the plan's list, unchanged: `TestVerifierCharacters` (generated by `VerifierTestBuilder`, reflection), `TestDocType`, `TestEntityRef`,
  `TestNamespaceAware`, `TestDescendantIterator`, `TestDescendantFilterIterator`, `TestJDOMExceptn`, `TestFilterList`, `TestFilterListText`,
  `TestFilterListElement`, `TestElementFilterList`, `util/ListTest` (these extend `AbstractTestList`: they pass `JvmClassMappingKt.getKotlinClass(X.class)`),
  `TestXMLOutputProcessor` (`java.io.Writer`), `TestArrayCopy`, `TestNamespaceStack`, `special/TestIssue173ContentCount` (keeps its
  `@Ignore` and `// K5: needs KdomBuilder (Task 9)` marker, ruling R4).

Differences from the Java tests (none changes an expectation):
- **R4 markers.** kotlin.test's `@Ignore` takes no argument, so the seven `TestXMLOutputter` tests read
  `// K5: needs KdomBuilder (Task 9)` then `@Ignore /* needs KdomBuilder, Task 9 */ @Test fun ...`, and keep the private placeholder
  `parseWithKdomBuilder(String)` (throws `UnsupportedOperationException`). Task 9 fills the placeholder and removes the `@Ignore`s. Their
  `ByteArrayOutputStream` output now goes through `outputToString` (the OutputStream form on the JVM through the `outputViaStream` hook, the
  Appendable form on iOS).
- **No reflection.** `TestXMLOutputter.checkOutput` found `outputString`/`output(Writer)`/the stream method by reflection; it dispatches on the
  content type now (`outputStringOf`, `outputToAppendable`), and the OutputStream form is the common `expect fun outputViaStream` (JVM actual in
  `jvmTest`: the `XMLOutputterJvm.kt` extensions; iOS actual: `null`, no stream form). `AbstractTestOutputter` replaced its table of class-keyed
  runners by a `when`; `checkOutput` lost its `Class` parameter.
- **`AbstractTestFilter` twins** (Task 7 review minor): `exercise`/`exerciseAtt` take a second, separately constructed filter that is equal
  to the first (`twin`) instead of a deserialized copy (which Task 7 had replaced by the same instance, losing the equal-but-not-identical
  case). It checks `twin !== af` and `assertFilterEquals(af, twin)`, exercises the twin, and builds every derived filter (`negate`,
  `or`, `and`, `refine`) a second time from it; `exerciseCore(ef, cf, ..)` takes the equal copy where `deSerialize(ef)` was used. The callers
  construct the twin (`ElementFilter(name)` twice, `ContentFilter(mask)` as the twin of the flag-by-flag `cfa`). `TestFilters#checkFilter` keeps
  its Task 7 form (no deserialized-copy equality line).
- **`AbstractTestList`** takes a `KClass<*>` (`isInstance`), `buildArray` copies the sample array (`copyOf`) instead of `Array.newInstance`,
  `ArrayCopy` calls are the stdlib `copyOf`/`copyOfRange`; a `null` that jdom2 passed as `(T) null`, or a `null` collection, is `nullT()`
  (`fi.sykero.kdom.internal.unsafeNull`: an erased null, so the callee's own null check is what is exercised). `testIllegalClassContent` returns
  at once when `checksErasedCasts` is false (`expect val` in `TestPlatform.kt`, true on the JVM, false on iOS): Kotlin/Native does not check an erased
  cast at the typed call, so the wrong-class object would enter the typed list unchecked; the other 18 inherited tests run on both platforms.
- Java statics became instance state (`private static final Element base`, the `static {}` format setup of `AbstractTestOutputter`): each test
  instance builds its own, which is what JUnit's per-method instances give anyway; `TestFormat.checkEquals` is a `@JvmStatic` companion
  function (`TestXMLOutputter` calls it). `LinkedList` became `ArrayList`, `Collections.EMPTY_LIST` `emptyList<Any?>()`, `String.format`
  string templates, Java `==` on references `===`, an `int` argument that took a `char` (`Verifier.isXMLCharacter('x')`) `'x'.code`, a
  `switch` a `when` (`Content.CType` entries), a `char[]` write `sb[i] = c`.
- Null passed where Java passed a typed `null` variable (`element.addContent((CDATA) null)`, `setAttribute(null)`): `nullT<Content>()` /
  `nullT()`, which reaches the callee's parameter check; the expected `NullPointerException` is unchanged.
- `xmlchars.xml` is read by none of the kept or translated tests, so nothing is embedded; the file stays in `src/jvmTest/resources`.

## Task 5 traps (met while translating)

- **Kotlin `protected` is stricter than Java `protected`.** Java `protected` is also package access, which `ContentList`,
  `AttributeList`, `Element`, `Document` relied on (`child.setParent(parent)`, `c.parent = parent`, `a.clone()` on `Content`
  receivers). Kotlin allows protected access only on a receiver of the subclass type, so these members became public (`clone()` overrides) or `internal` (`setParent`, `@JvmField internal` `parentRef`).
- **A property named like a field.** `Content.parent`, `Element.content/attributes/additionalNamespaces`, `Document.content` were fields
  *and* getter names; the property rule needs the name, so the fields were renamed (above). Keep the JVM field names of the
  serialized fields (`name`, `namespace`, `value`, `type`, `specified`, `ctype`, `elementName`, ...) so default serialization is
  unchanged: that is why `Attribute.type/specified` are private fields with `attributeType`/`isSpecified` accessors.
- **`size` inside an inner class.** In `ContentList`, `FilterList` (an inner class that is itself a list) and the iterators referred to the
  outer field `size`. In Kotlin the `FilterList.size` property would shadow it, so the outer field is `count`.
- **`AbstractMutableList.modCount`** exists on the JVM (inherited from `java.util.AbstractList`) and is accessible from the subclass and
  its inner classes. Task 7: the common `AbstractMutableList` declares it too (compiled for iOS), nothing had to be re-declared;
  `ContentList` does not use it (own counters).
- **`sort` override is invisible to Kotlin callers**, see API shape; calling `contentList.sort(cmp)` resolves to the stdlib extension.
  Task 7 moved the override into the JVM actual of `KdomMutableList`.
- **Overloaded `filter(List)` vs `filter(Object)`** are chosen by static type, as in Java; call sites that hold `Any?` pick the right one
  (`AbstractFilter.matches`, `ContentList.FilterList.resync`, `FilterIterator`).
- **`Boolean::class.java` is the primitive `boolean` class** (`isInstance` is always false): `Filters.fboolean()` had to wrap
  `java.lang.Boolean::class.java`. Task 7: `ClassFilter` holds a `KClass`, whose `isInstance` handles the boxed values.
- **Identity comparisons**: `Text.value === EMPTY_STRING` (`CDATA.append`), `ns === Namespace.NO_NAMESPACE`, `tfirst.parent !== text.parent`
  (`Element.coalesceText`), `(left + right) ushr 1`, `~x` -> `.inv()`; `String.trim()` -> `trim { it <= ' ' }` (`Element.getTextTrim`,
  `Attribute.get*Value`, `ProcessingInstruction.parseData`).
- **`Document.setProperty(null, v)`**: jdom2's `HashMap` accepts a null key; the map is `HashMap<String?, Any?>`.
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

(Task 7: all of these are gone from the kdom sources; the only JVM-only files are `internal/Platform.jvm.kt` and
`output/XMLOutputterJvm.kt`.)

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
- Task 7 (serialization removed): `TestDocument#test_TCU__testSerialization`, `TestElement#test_TCU__testSerialization`,
  `TestAttribute#serialization_default`, `TestAttribute#serialization_Namespace`, `TestProcessingInstruction#testSerialize`.
- Task 7 (`Element.getXMLBaseURI` removed): `TestElement#testXmlBaseNone`, `#testXmlBaseDocument`, `#testXmlBaseRelative`,
  `#testXmlBaseRelativeToDoc`, `#testXmlBaseAbsolute`, `#testXmlBaseSubAbsolute`, `#testXmlBaseBroken`.
- Task 7, changed but kept (marked `// K5:`): `AbstractTestFilter#exercise` lost its deserialized-copy round and uses the filter itself
  where it used `UnitTestUtil.deSerialize(af)` (also in `exerciseAtt` and the `refine` checks); `TestFilters#checkFilter` lost the
  deserialized-copy equality line and calls `fclass` with a `KClass`; `TestContentFilter#testEqualsObject` compares with a new equal
  `ContentFilter` instead of a deserialized copy; `TestFormat#testCharsetEncodingEscapes` / `#testIllegalEncodingEscapes` expect the
  kdom escape answers; `TestXMLOutputter` reaches the `OutputStream` overloads through `XMLOutputterJvmKt` and the writer overloads
  through `Appendable`. `UnitTestUtil.deSerialize` is unused (kept, it compiles for any `Serializable`).
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
0 errors. After Task 4 fix round 1: 41 test classes, 708 tests (+4 `JDOMParseExceptionTest`), 10 skipped, 0 failures. After Task 5 (tree core and filters in Kotlin): 41 classes / 708 tests / 10 skipped / 0 failures; after fix round 1 (R8): 42 classes / 712 tests (+4 `PropertySyntaxTest`) / 10 skipped / 0 failures. After Task 6 (output package in Kotlin, no Java left in `src/jvmMain/java`): 43 classes / 716 tests (+4 `OutputDifferentialTest`) / 10 skipped / 0 failures. After Task 7 (commonized): 50 classes / 724 tests / 10 skipped / 0 failures
(716 - 5 serialization tests - 7 `getXMLBaseURI` tests + 20 new: `NamespaceCacheConcurrencyTest` 3 (common), `FormatEscapeStrategyTest` 2,
`LineSeparatorTest` 2, `CloneTest` 5, `CommonCollectionsTest` 3, `CommonHelpersTest` 3, `NullSetterTest` 2).
This number may only grow (apart from the listed removals); the 8 K5 ignores return in Task 9.
After Task 8 (common tests; 5 raw-instance tests moved into `RawInstanceTest`, the totals of the translated classes are unchanged): 52 classes / 728 tests
(724 + `LineSeparatorTest#defaultIgnoresTheJdom2SystemProperty` + 3 `OutputStreamBindingTest`) / 10 skipped / 0 failures.
After Task 9 (XmlEventParser, KdomBuilder): 58 classes / 766 tests / 2 skipped / 0 failures (728 + 38 new in 6 classes:
`XmlEventParserProbeTest` 4, `SaxEventDifferentialTest` 5, `KdomBuilderTest` 23, `KdomBuilderDifferentialTest` 2, `KdomBuilderJvmTest` 1,
`KdomBuilderConcurrencyTest` 3; the 8 K5 `@Ignore`s are re-enabled, leaving jdom2's own 2 skipped).
After Task 9 fix round 1: 58 classes / 773 tests / 2 skipped / 0 failures (+5 `KdomBuilderTest`, +1 `XmlEventParserProbeTest`,
+1 `KdomBuilderDifferentialTest`; new cases added to the existing differential lists).

After Task 11 (query axes): 62 classes / 808 tests / 2 skipped / 0 failures (+`AxesTest` 10, +`AxesJaxenDifferentialTest` 2; fix round 1 added one `AxesTest`).

### Import notes (fix round 1)

- `TestDescendantIterator`, `TestDescendantFilterIterator` and `TestJDOMExceptn` (17 tests) use only imported classes and are
  imported unchanged under `test/cases/`; all run green (none ignored).
- Line endings: the CRLF originals of `UnitTestUtil.java` and `complex.xml` were normalised to LF by git.
- `xmlchars.xml` and `complex.xml` are copied to `src/jvmTest/resources` but are unused by the kept and the translated tests
  (Task 8: nothing to embed, see "Common tests").
- Known stale string: `output/LineSeparator.java` javadoc/comment (lines ~109, ~171) still said
  `-Dorg.jdom2.output.LineSeparator`; Task 7 removed it together with `SystemProperty`.

## Task 10 (SAX bridge)

- `fi.sykero.kdom.sax.KdomEventProvider(Document)` / `(Element)` replays a tree as the event stream of jdom2's
  `SAXOutputter(handler).output(doc)` with defaults (the `JDOMSAXEventProvider` setup); proven by
  `KdomEventProviderDifferentialTest` (19 cases, event strings incl. per-call `characters` start/length/array size,
  attribute probes by index and both name lookups). `DefaultHandler` and `AttributesImpl` mirror the JDK classes.
- Rules found: `setDocumentLocator` once first (locator values -1/-1/null/null, never null), `startDocument`; only top-level
  Element and ProcessingInstruction are reported (Comment, DocType dropped); `startPrefixMapping` for `NamespaceStack.addedForward()`
  (element's namespace first), `endPrefixMapping` in `addedReverse()` order after `endElement`; no `xmlns` attributes;
  attribute type `CDATA` for UNDECLARED else the enum name; `characters` once per Text/CDATA node with a fresh exact-size array
  at offset 0, also for empty Text; EntityRef -> `skippedEntity`; comments inside elements dropped; `ignorableWhitespace` never.
- `KdomEventProvider(Element)` is `element.document ?: Document(element)`: a detached element is wrapped (a parent-less element is
  required), an attached one reports its whole document and is not detached.
- The handler parameters are non-null (`prefix: String`, ...), unlike the JDK's platform types.

## Query axes (Task 11, new code)

`fi.sykero.kdom.query` (`Axes.kt`) replaces the XPath expressions `:app` evaluated through jdom2/jaxen. Differentially tested
against jaxen (`AxesJaxenDifferentialTest`: every expression from every context node, attached and detached).

- Absolute `//` from a detached tree is empty, as with jaxen on jdom2: `Parent.xpathRoot()` is `Document?` (a Document itself,
  an attached element's Document, null for a detached element or any node of a detached subtree) and `findAll` returns an empty
  list when there is no root. Callers that need the descendants of a detached element use `descendantElements()`; relative axes
  (`.//x`, `../x`, `descendant::`, `ancestor::`, siblings) work on detached trees exactly as in jaxen (ruling R10).
- Name tests compare localName and namespace URI (never the prefix); `NO_NAMESPACE` matches no-namespace elements only, so the
  unprefixed XPath name `note` is `isNamed("note", Namespace.NO_NAMESPACE)`.
- Multi-step paths (`//a/b`) are composed by filtering the document-ordered `allElements()` on the element and its
  `parentElement`, never by `flatMap { childElements() }` over the parents (nested parents would be grouped by parent and
  lose document order, which jaxen keeps); `findChildrenOf` is that composition.
- Sequences are lazy; a caller that mutates the tree while iterating (e.g. `SwordContentFacade.addAnchors`) must `.toList()`
  first, as jaxen returned an eager list.
- `precedingSiblingElements()` and `ancestorElements()` are nearest first (XPath reverse axes); the rest is document order.

# kdom

kdom (`fi.sykero.kdom`) is a Kotlin Multiplatform XML tree library: a translation of the subset of
JDOM 2.0.6.1 that JSword and AndBible use, plus new code (an event-based parser and builder on
xmlutil, a SAX-shaped bridge and query axes). It preserves the characterized jdom2 2.0.6.1 behavior
within that subset, with explicit API/platform differences listed in `docs/translation-notes.md`.

Targets: `jvm`, `iosArm64`, `iosSimulatorArm64`, plus `linuxX64` for running the common tests natively
on Linux (not shipped; `nativeMain`/`nativeTest` hold the actuals it shares with iOS). `commonMain` depends only on the Kotlin stdlib and
xmlutil (`io.github.pdvrieze.xmlutil:core:1.0.2.1`). It has no JSword or AndBible production dependency.
Test-only (`jvmTest`): JUnit 4, jdom2 and jaxen as oracles.

## Package map (1:1 with jdom2)

| jdom2 | kdom |
| --- | --- |
| `org.jdom2` | `fi.sykero.kdom` |
| `org.jdom2.filter` | `fi.sykero.kdom.filter` |
| `org.jdom2.output` | `fi.sykero.kdom.output` |
| `org.jdom2.output.support` | `fi.sykero.kdom.output.support` |
| `org.jdom2.internal` | `fi.sykero.kdom.internal` |
| `org.jdom2.util` | `fi.sykero.kdom.util` |
| `org.jdom2.input.JDOMParseException` | `fi.sykero.kdom.input.JDOMParseException` |

New: `fi.sykero.kdom.input.KdomBuilder`, `fi.sykero.kdom.input.XmlEventParser`, `fi.sykero.kdom.sax.*`,
`fi.sykero.kdom.query.*`. Class names stay jdom2's (including `JDOMException`).

## Property rule

Every jdom2 public no-argument `getX()`/`isX()` is a Kotlin property `x`/`isX` (`var` when jdom2 has
a single-argument `setX`). Overloads with parameters stay functions (`getChildren(name)`,
`setAttribute(name, value)`).

Dual setter API: a fluent jdom2 setter is kept as jdom2 shaped it, `open fun setX(v): Self` (returns the
receiver, so `Element("a").setText("1")` chains and Java callers are unchanged), **and** the property is a
`var` whose setter (`@JvmSynthetic`, hidden from Java) calls `setX`, so Kotlin can write `e.text = "1"`.
Void jdom2 setters are plain `var` properties. Exceptions and details are listed in
`docs/translation-notes.md` ("Property rule exceptions").

## Parsing and DOCTYPE policy

`KdomBuilder` (jdom2 `SAXBuilder` replacement) and `XmlEventParser` (SAX-shaped events) read XML with
xmlutil. In the default **strict** mode the differential corpus and mutation fuzz pin rejection against
the JDK's JAXP SAX parser (not a proof for all possible XML); known accept/reject differences include that a
DOCTYPE is always rejected and that a malformed `encoding` name in the XML declaration of a *string*
input is rejected, while the JDK ignores it. In strict mode any DOCTYPE is a parse error, which is what JSword's OSIS/ThML/GBF parsing relies on (a malformed verse keeps
taking its fallback chain). For EPUB XHTML/OPF/NCX and other non-Sword XML, `KdomBuilder(allowDocType =
true)` / `XmlEventParser(namespaceAware, allowDocType = true)` accept a DOCTYPE and build a `DocType`
node as jdom2 does; the external subset is never loaded (no network, no files), internal-subset
entities are expanded, and an undeclared entity under an unread external subset becomes an
`EntityRef`. Known permissive-mode gaps: `ATTLIST` declarations, parameter-entity references and
entities whose replacement text holds markup or non-predefined references are rejected (the JDK accepts
them); internal-subset processing instructions are dropped; relative system ids of internal-subset
declarations are kept as written. Details: `docs/xmlutil-probes.md` ("DOCTYPE policy") and
`docs/translation-notes.md`.

## Compatibility boundary

This is the used subset, not a drop-in implementation of every JDOM API. In particular:

- Common output takes `Appendable`; `OutputStream` output is a JVM extension (import
  `fi.sykero.kdom.output.output`). A `PrintStream` selects the Appendable member and uses its own encoding,
  not `Format.encoding` (`OutputStreamBindingTest`).
- Java serialization, `SystemProperty` and `Element.getXMLBaseURI()` were removed. Clone uses explicit
  copies; an unknown subclass clones to its kdom base class (`CloneTest.subclassClonesToKdomClass`).
- Some members are final where JDOM allowed overriding; getter JVM names, null handling, raw protected
  constructors and fluent setters have specific exceptions. See the full inventory in the translation notes.
- `fi.sykero.kdom.query` supplies axes/name tests, not an XPath evaluator. Absolute queries require a
  document; detached trees need relative axes. Lazy sequences must be materialized before mutation
  (`AxesTest`, `AxesJaxenDifferentialTest`).
- Native compilation is not Native runtime verification. Raw-instance non-null/null tricks, live-list
  `sortWith`, unusual floating-point grammar and platform diagnostic names have documented limits.
  Use `Element.sortContent/sortChildren/sortAttributes` for portable live-list sorting.

The translation notes retain historical baseline counts separately from current behavior; they list
removed members, API-shape exceptions, parser/output differences, test references and unverified cases.

## Build

```
./gradlew jvmTest
./gradlew linuxX64Test     # Kotlin/Native behaviour without a Mac (e.g. no implicit null checks)
./gradlew compileKotlinIosSimulatorArm64
```

## Licence

JDOM licence, see `LICENSE` and `NOTICE`. The licence (clause 4) forbids "JDOM" in the name of a
derived product, so the library name must never contain "JDOM".

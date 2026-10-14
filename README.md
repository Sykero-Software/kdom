# kdom

kdom (`fi.sykero.kdom`) is a Kotlin Multiplatform XML tree library: a translation of the subset of
JDOM 2.0.6.1 that JSword and AndBible use, plus new code (an event-based parser and builder on
xmlutil, a SAX-shaped bridge and query axes). Behaviour stays that of jdom2 2.0.6.1; differences are
listed in `docs/translation-notes.md`.

Targets: `jvm`, `iosArm64`, `iosSimulatorArm64`. `commonMain` depends only on the Kotlin stdlib and
xmlutil (`io.github.pdvrieze.xmlutil:core`). Test-only (`jvmTest`): JUnit 4, jdom2 and jaxen as oracles.

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
`setAttribute(name, value)`). A fluent jdom2 setter becomes the property setter and returns `Unit`.
Exceptions are listed in `docs/translation-notes.md`.

## Build

```
./gradlew jvmTest
./gradlew compileKotlinIosSimulatorArm64
```

## Licence

JDOM licence, see `LICENSE` and `NOTICE`. The licence (clause 4) forbids "JDOM" in the name of a
derived product, so the library name must never contain "JDOM".

#!/usr/bin/env python3
"""Imports the used jdom2 2.0.6.1 subset (and jdom2's own tests) into kdom as Java, package-renamed.

Provenance: https://github.com/hunterhacker/jdom tag JDOM-2.0.6.1 (commit c6065f684bdff5cebf5f23b2859c5e4c89fd89db).
Usage: python3 -I scripts/import-jdom2.py <jdom-clone-dir> <kdom-dir>
The clone is untrusted data; this script only reads files from it. Idempotent: rewrites the target files.
"""
import os
import re
import sys

LINE = "Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software."

MAIN = """Attribute AttributeList AttributeType CDATA CloneBase Comment Content ContentList DataConversionException
DescendantIterator DocType Document Element EntityRef FilterIterator IllegalAddException IllegalDataException
IllegalNameException IllegalTargetException JDOMConstants JDOMException Namespace NamespaceAware Parent
ProcessingInstruction Text Verifier
filter/AbstractFilter filter/AndFilter filter/AttributeFilter filter/ClassFilter filter/ElementFilter filter/Filter
filter/Filters filter/NegateFilter filter/OrFilter filter/PassThroughFilter filter/TextOnlyFilter
input/JDOMParseException internal/ArrayCopy internal/SystemProperty
output/EscapeStrategy output/Format output/LineSeparator output/XMLOutputter
output/support/AbstractFormattedWalker output/support/AbstractOutputProcessor output/support/AbstractXMLOutputProcessor
output/support/FormatStack output/support/Walker output/support/WalkerNORMALIZE output/support/WalkerPRESERVE
output/support/WalkerTRIM output/support/WalkerTRIM_FULL_WHITE output/support/XMLOutputProcessor
util/IteratorIterable util/NamespaceStack""".split()

# (path under test/src/java/org/jdom2, or glob dir marker "dir/*")
TESTS = []
for n in """TestAttribute TestAttributeList TestCDATA TestComment TestContentList TestDocType TestDocument TestElement
TestElementFilterList TestEntityRef TestFilterList TestFilterListElement TestFilterListText TestNamespace
TestNamespaceAware TestProcessingInstruction TestText TestVerifier TestVerifierCharacters""".split():
    TESTS.append("test/cases/" + n)
TESTS.append("test/cases/filter/*")
for n in "AbstractTestOutputter TestFormat TestXMLOutputProcessor TestXMLOutputter".split():
    TESTS.append("test/cases/output/" + n)
TESTS += ["test/cases/util/TestArrayCopy", "test/cases/util/TestNamespaceStack", "test/cases/special/TestIssue173ContentCount"]
for n in "AbstractTestList ListTest UnitTestUtil VerifierTestBuilder".split():
    TESTS.append("test/util/" + n)
TESTS += ["TestIllegalAddExceptn", "TestIllegalNameExceptn", "TestIllegalTargetExceptn"]

RESOURCES = ["xmlchars.xml", "DOMBuilder/complex.xml"]


def rewrite(text):
    text = re.sub(r"\borg\.jdom2\b", "fi.sykero.kdom", text)
    if LINE in text:
        return text
    idx = text.find("\n */")
    if text.lstrip().startswith("/*--") and idx > 0:
        return text[:idx] + "\n " + LINE + "\n" + text[idx:]
    return "/* " + LINE + " */\n" + text


# Imports that existed only for javadoc and name excluded classes / javax.xml (see docs/translation-notes.md).
CUT_LINES = {
    "JDOMConstants.java": [
        "import javax.xml.transform.TransformerFactory;",
        "import fi.sykero.kdom.transform.JDOMResult;",
        "import fi.sykero.kdom.transform.JDOMSource;",
        "import fi.sykero.kdom.xpath.XPathFactory;",
    ],
}


def cut(name, text):
    for line in CUT_LINES.get(name, []):
        assert line + "\n" in text, (name, line)
        text = text.replace(line + "\n", "", 1)
    return text


def copy(src, dst):
    with open(src, encoding="utf-8") as f:
        text = f.read()
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    with open(dst, "w", encoding="utf-8", newline="") as f:
        f.write(cut(os.path.basename(dst), rewrite(text)) if "/jvmMain/" in dst else rewrite(text))


def main():
    clone, kdom = sys.argv[1], sys.argv[2]
    core = os.path.join(clone, "core/src/java/org/jdom2")
    tsrc = os.path.join(clone, "test/src/java/org/jdom2")
    jmain = os.path.join(kdom, "src/jvmMain/java/fi/sykero/kdom")
    jtest = os.path.join(kdom, "src/jvmTest/java/fi/sykero/kdom")
    for m in MAIN:
        copy(os.path.join(core, m + ".java"), os.path.join(jmain, m + ".java"))
    for t in TESTS:
        if t.endswith("/*"):
            d = t[:-2]
            for fn in sorted(os.listdir(os.path.join(tsrc, d))):
                copy(os.path.join(tsrc, d, fn), os.path.join(jtest, d, fn))
        else:
            copy(os.path.join(tsrc, t + ".java"), os.path.join(jtest, t + ".java"))
    for r in RESOURCES:
        dst = os.path.join(kdom, "src/jvmTest/resources", r)
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        with open(os.path.join(clone, "test/src/resources", r), "rb") as f:
            data = f.read()
        with open(dst, "wb") as f:
            f.write(data)
    print("imported", len(MAIN), "main classes")


main()

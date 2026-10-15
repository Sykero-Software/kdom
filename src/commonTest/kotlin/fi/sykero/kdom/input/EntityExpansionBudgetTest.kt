package fi.sykero.kdom.input

import fi.sykero.kdom.sax.SAXParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Small inputs amplify to at most one million code units, never an exhaustion payload. */
class EntityExpansionBudgetTest {
    private fun xml(value: String, body: String) = "<!DOCTYPE a [<!ENTITY e '$value'>]><a$body"
    private val literal = "x".repeat(1000)

    @Test fun repeatedLiteralAtAndBelowBudgetRetainsText() {
        val builder = KdomBuilder(allowDocType = true)
        // Definition storage (1000) plus 999 uses (999000) exactly fills the budget.
        for (count in listOf(998, 999)) {
            val text = builder.build(xml(literal, ">${"&e;".repeat(count)}</a>")).rootElement.text
            assertEquals(count * 1000, text.length)
            assertTrue(text.all { it == 'x' })
        }
    }

    @Test fun repeatedLiteralOverBudgetIsControlledAndBuilderReusable() {
        val builder = KdomBuilder(allowDocType = true)
        val error = assertFailsWith<JDOMParseException> {
            builder.build(xml(literal, ">${"&e;".repeat(1000)}</a>")); Unit
        }
        assertIs<SAXParseException>(error.cause)
        assertTrue(error.message!!.contains("1000000"))
        assertEquals("ok", builder.build(xml("ok", ">&e;</a>")).rootElement.text)
    }

    @Test fun attributesNamespacesAndContentShareBudget() {
        val builder = KdomBuilder(allowDocType = true)
        // Each channel alone fits; their combined replacement text does not.
        for (attribute in listOf("v", "xmlns:p")) {
            val error = assertFailsWith<JDOMParseException> {
                builder.build(xml(literal, " $attribute='${"&e;".repeat(500)}'>${"&e;".repeat(500)}</a>"))
                Unit
            }
            assertIs<SAXParseException>(error.cause)
            assertTrue(error.message!!.contains("1000000"))
        }
    }

    @Test fun parserBudgetResetsAfterRejection() {
        val parser = XmlEventParser(allowDocType = true)
        assertFailsWith<SAXParseException> {
            parser.parse(xml(literal, ">${"&e;".repeat(1000)}</a>"), fi.sykero.kdom.sax.DefaultHandler())
        }
        var length = 0
        parser.parse(xml(literal, ">${"&e;".repeat(999)}</a>"), object : fi.sykero.kdom.sax.DefaultHandler() {
            override fun characters(ch: CharArray, start: Int, count: Int) { length += count }
        })
        assertEquals(999000, length)
    }

    @Test fun definitionDecodingIsBoundedEvenWithoutUses() {
        val error = assertFailsWith<JDOMParseException> {
            KdomBuilder(allowDocType = true).build(xml("&#65;".repeat(1_000_001), "/>"))
            Unit
        }
        assertIs<SAXParseException>(error.cause)
        assertTrue(error.message!!.contains("1000000"))
    }

    @Test fun oneUnitOverCombinedBudgetIsRejected() {
        val value = "x".repeat(500000)
        val builder = KdomBuilder(allowDocType = true)
        val atLimit = builder.build(xml(value, " v='&e;'/>"))
        assertEquals(500000, atLimit.rootElement.getAttributeValue("v")!!.length)
        assertFailsWith<JDOMParseException> {
            builder.build("<!DOCTYPE a [<!ENTITY e '$value'><!ENTITY f 'x'>]><a v='&e;'/>")
            Unit
        }
    }

    @Test fun predefinedReplacementReferencesAreChargedByDecodedLength() {
        val builder = KdomBuilder(allowDocType = true)
        val value = "&amp;".repeat(1000)
        assertEquals(995000, builder.build(xml(value, ">${"&e;".repeat(995)}</a>")).rootElement.text.length)
        assertFailsWith<JDOMParseException> {
            builder.build(xml(value, ">${"&e;".repeat(996)}</a>"))
            Unit
        }
    }

    @Test fun supplementaryCharactersCountAsTwoUtf16Units() {
        val value = "😀".repeat(1000)
        val builder = KdomBuilder(allowDocType = true)
        assertEquals(998000, builder.build(xml(value, ">${"&e;".repeat(499)}</a>")).rootElement.text.length)
        assertFailsWith<JDOMParseException> {
            builder.build(xml(value, ">${"&e;".repeat(500)}</a>"))
            Unit
        }
    }
}

package fi.sykero.kdom.probe

/** The probe inputs shared by XmlUtilReaderProbeTest (commonTest) and SaxReferenceProbeTest (jvmTest). */
object ProbeInputs {
    val probes: Map<String, String> = linkedMapOf(
        "unclosed root" to "<a><b></b>",
        "stray ampersand" to "<a>x & y</a>",
        "undefined entity" to "<a>&nbsp;</a>",
        "unquoted attribute" to "<a b=1/>",
        "unterminated attribute value" to "<a b=\"1/>",
        "mis-nested tags" to "<a><b><c></b></c></a>",
        "stray end tag" to "<a></b></a>",
        "text after root" to "<a/>tail",
        "two roots" to "<a/><b/>",
        "duplicate attribute" to "<a b=\"1\" b=\"2\"/>",
        "less-than in text" to "<a>1 < 2</a>",
        "empty input" to "",
        "undeclared prefix" to "<p:a/>",
        "valid control" to "<a x=\"&lt;&amp;&#233;\"><![CDATA[<raw>]]>t&gt;</a>",
        // added in fix round 1
        "undefined entity in attribute" to "<a x=\"&nbsp;\"/>",
        "illegal character" to "<a>\u0001</a>",
        "less-than in attribute" to "<a x=\"<\"/>",
        "doctype with entity decl" to "<!DOCTYPE a [<!ENTITY e \"v\">]><a>&e;</a>",
        "plain doctype" to "<!DOCTYPE a><a/>",
        "out-of-range character reference" to "<a>&#1114112;</a>",
    )
}

package com.qust.helper.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlUtilsTest {

    @Test
    fun escapeHtml_basicTags() {
        val input = "<script>alert('xss')</script>"
        val expected = "&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"
        assertEquals(expected, HtmlUtils.escapeHtml(input))
    }

    @Test
    fun escapeHtml_entities() {
        val input = "a & b < c > d \"e\" 'f'"
        val expected = "a &amp; b &lt; c &gt; d &quot;e&quot; &#39;f&#39;"
        assertEquals(expected, HtmlUtils.escapeHtml(input))
    }

    @Test
    fun escapeHtml_plainText() {
        val input = "正常的课程名称"
        assertEquals(input, HtmlUtils.escapeHtml(input))
    }

    @Test
    fun escapeHtml_emptyString() {
        assertEquals("", HtmlUtils.escapeHtml(""))
    }
}

package net.osjava.qframe.web

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class IResultTest {
    @Test
    internal fun `Result view path`() {
        val result = IResult.render(ViewPath("/view_path").with("key", "value"))
        println(result.data.url)
    }
}
package net.osjava.qframe.utils

import net.osjava.qframe.utils.PathUtils.concat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PathUtilsTest {

    @Test
    fun concat() {
        var path = concat("/", "/sub1", "sub2/", "/sub3/", "sub4")
        assertEquals("/sub1/sub2/sub3/sub4", path)

        path = concat("/root", "/sub1", "sub2/", "/sub3/", "sub4")
        assertEquals("/root/sub1/sub2/sub3/sub4", path)

        path = concat("/", "/")
        assertEquals("/", path)

        path = concat("/root", "/")
        assertEquals("/root", path)
    }
}
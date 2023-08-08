package net.osjava.qframe.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class AppConfigTest {
    private val workspace = File("src/test/workspace").absolutePath

    @Test
    internal fun `Configuration Initialize`() {
        println("Test Workspace=$workspace")

        AppConfig.initialize(appId = "unit-test", workspace = workspace)
        assertEquals("unit-test", AppConfig.appId)
        assertEquals(AppConfig.AppEnv.DEVELOP, AppConfig.appEnv)
        assertEquals(workspace, AppConfig.workspace)
    }
}
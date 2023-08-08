package net.osjava.qframe.test

import net.osjava.qframe.core.AbstractApplication
import net.osjava.qframe.utils.PathUtils
import org.springframework.test.context.TestContext
import org.springframework.test.context.support.AbstractTestExecutionListener

class AppTestExecutionListener : AbstractTestExecutionListener() {
    override fun beforeTestClass(testContext: TestContext) {
        val rootPath = System.getProperty("user.dir")

        var homePath: String = PathUtils.concat(rootPath, "/src/test/workspace")

        if (!PathUtils.isDirectory(homePath)) {
            homePath = PathUtils.concat(rootPath, "/src/main/workspace")
        }

        if (!PathUtils.isDirectory(homePath)) {
            throw RuntimeException("Workspace directory is not exist.")
        }

        println("Init application for testing: homePath=$homePath")

        TestApplication(homePath)
    }

    class TestApplication(homePath: String) : AbstractApplication() {
        init {
            initialize("UnitTest", homePath)
        }
    }
}
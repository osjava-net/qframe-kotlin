package net.osjava.qframe.core

import net.osjava.qframe.utils.HttpUtils
import net.osjava.qframe.utils.PathUtils
import org.apache.logging.log4j.core.config.Configurator
import java.util.*
import java.util.logging.Logger

abstract class AbstractApplication {
    private val log: Logger = Logger.getLogger("net.osjava.qframe.core.AbstractApplication")

    protected fun initialize(appId: String, workspace: String, extraProperties: Properties? = null) {
        log.info("Initialize application...")

        Configurator.initialize(appId, PathUtils.concat(workspace, "/conf/log4j-config.xml"))

        AppConfig.initialize(appId, workspace, extraProperties)

        BeanFactory.initialize()

        HttpUtils.initialize()

        log.info("Initialize application... DONE")
    }

    protected fun destroy() {
        HttpUtils.destroy()
        log.info("Destroy application ${AppConfig.appId}... DONE")
    }
}
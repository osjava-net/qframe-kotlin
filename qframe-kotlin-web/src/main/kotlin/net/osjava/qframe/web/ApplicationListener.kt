package net.osjava.qframe.web

import net.osjava.qframe.core.AbstractApplication
import net.osjava.qframe.core.AppConfig
import net.osjava.qframe.utils.PathUtils
import org.slf4j.LoggerFactory
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

class ApplicationListener : AbstractApplication(), ServletContextListener {
    private val log = LoggerFactory.getLogger(ApplicationListener::class.java)

    override fun contextInitialized(sce: ServletContextEvent) {
        val contextPath = when (sce.servletContext.contextPath) {
            "/" -> "/root"
            else -> sce.servletContext.contextPath
        }

        val appId = sce.servletContext.getInitParameter("application-id")
                ?: "application-${contextPath.replace("/", "")}"

        sce.servletContext.getInitParameter("workspace")?.let {
            log.debug("Initialize web application: workspace=$it")
            initialize(appId, it)
            return
        }

        System.getProperty("WORKSPACE_${appId}")?.let {
            log.debug("Initialize web application: workspace=$it")
            initialize(appId, it)
            return
        }

        System.getProperty("WEBAPP_WORKSPACES")?.let {
            log.debug("Initialize web application: workspace=$it")
            initialize(appId, PathUtils.concat(it, contextPath))
            return
        }

        System.getenv("WEBAPP_WORKSPACES")?.let {
            log.debug("Initialize web application: workspace=$it")
            initialize(appId, PathUtils.concat(it, contextPath))
            return
        }

        initialize(appId, sce.servletContext.getRealPath("/WEB-INF"))
        log.debug("Initialize web application: workspace=${AppConfig.workspace}")
    }

    override fun contextDestroyed(sce: ServletContextEvent) {
        super.destroy()
    }

}
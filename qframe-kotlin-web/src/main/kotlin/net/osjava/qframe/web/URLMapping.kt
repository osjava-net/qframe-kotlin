package net.osjava.qframe.web

import net.osjava.qframe.core.*
import net.osjava.qframe.core.Constants.CTX_ACTION_SERVLET
import net.osjava.qframe.core.Constants.CTX_VIEW_SERVLET
import net.osjava.qframe.core.Constants.REQUEST_ACTION_NAME
import net.osjava.qframe.utils.PathUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class URLMapping : Filter {
    private val log = LoggerFactory.getLogger(javaClass)

    private var actionServlet = "/action"
    private var viewServlet = "/view"
    private var ignoreList = emptyList<String>()
    private var cacheActions = emptyList<String>()

    override fun destroy() {

    }

    override fun init(filterConfig: FilterConfig) {
        log.debug("Init URLMapping...")

        filterConfig.getInitParameter("action")?.trim()?.let { actionServlet = it }
        filterConfig.getInitParameter("view")?.trim()?.let { viewServlet = it }
        filterConfig.getInitParameter("ignore")?.let { ignoreList = it.split(",").map { v -> v.trim() } }

        filterConfig.servletContext.setAttribute(CTX_ACTION_SERVLET, actionServlet)
        filterConfig.servletContext.setAttribute(CTX_VIEW_SERVLET, viewServlet)

        cacheActions = BeanFactory.getBeansWithAnnotation(Controller::class).keys.sortedByDescending { it.length }
        if (log.isDebugEnabled) {
            log.debug("Cache action map...")
            cacheActions.forEach { log.debug("|- {}", it) }
        }

        log.debug("Init URLMapping... DONE")
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        val requestUrl = when (httpRequest.contextPath) {
            "/" -> httpRequest.requestURI
            else -> httpRequest.requestURI.substringAfter(httpRequest.contextPath)
        }

        log.debug("Request URI: $requestUrl")

        if (hasIgnore(requestUrl)) {
            chain.doFilter(request, response)
            return
        }

        val rc = RequestContext.create(request = httpRequest,
                                       response = httpResponse,
                                       context = request.servletContext)
        try {
            if (requestUrl.startsWith("$actionServlet/") || requestUrl.startsWith("$viewServlet/")) {
                chain.doFilter(request, response)
                return
            }

            for (item in cacheActions) {
                if (requestUrl.startsWith(item)) {
                    rc.set(REQUEST_ACTION_NAME, item)
                    rc.forward(PathUtils.concat(actionServlet, requestUrl))
                    return
                }
            }

            rc.forward(PathUtils.concat(viewServlet, requestUrl))
        } finally {
            rc.destroy()
        }
    }

    private fun hasIgnore(uri: String): Boolean {
        for (item in ignoreList) {
            when {
                item.startsWith("*") && uri.endsWith(item.removePrefix("*"), true) -> {
                    log.debug("The uri[$uri] is in the ignore list: [$item]")
                    return true
                }
                item.endsWith("*") && uri.startsWith(item.removeSuffix("*"), true) -> {
                    log.debug("The uri[$uri] is in the ignore list: [$item]")
                    return true
                }
                uri.contains(item, true) -> {
                    log.debug("The uri[$uri] is in the ignore list: [$item]")
                    return true
                }
            }
        }
        return false
    }
}
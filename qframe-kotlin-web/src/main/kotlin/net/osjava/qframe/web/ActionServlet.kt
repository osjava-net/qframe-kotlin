package net.osjava.qframe.web

import net.osjava.qframe.core.BeanFactory
import net.osjava.qframe.core.Constants.CTX_VIEW_SERVLET
import net.osjava.qframe.core.Constants.DEFAULT_ACTION_METHOD
import net.osjava.qframe.core.Constants.REQUEST_ACTION_METHOD
import net.osjava.qframe.core.Constants.REQUEST_ACTION_NAME
import net.osjava.qframe.utils.ClassUtils
import net.osjava.qframe.utils.PathUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ActionServlet : HttpServlet() {
    private val log = LoggerFactory.getLogger(ActionServlet::class.java.enclosingClass)

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        doService(req)
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        doService(req)
    }

    private fun doService(req: HttpServletRequest) {
        val rc = RequestContext.get()

        val view = rc.context.getAttribute(CTX_VIEW_SERVLET) as String? ?: throw ServletException("Not found the view servlet")

        val actionName = rc.get(REQUEST_ACTION_NAME) ?: throw ServletException("Not found the action name")
        val requestUri = req.requestURI.substringAfter(req.servletPath)
        log.debug("Execute Action: name={}, uri={}", actionName, requestUri)

        val action = BeanFactory.getBean(actionName, AbstractAction::class)
        if (action == null) {
            rc.forward(PathUtils.concat(view, requestUri))
            return
        }

        val params = StringUtils.split(requestUri.substringAfter(actionName), "/")
        val methodName = if (params.isNotEmpty()) params[0] else DEFAULT_ACTION_METHOD

        ClassUtils.getMethod(action::class.java, methodName, RequestContext::class.java)?.run {
            rc.set(REQUEST_ACTION_METHOD, this.name)
            params.run { this.drop(0) }
        } ?: ClassUtils.getMethod(action::class.java, DEFAULT_ACTION_METHOD, RequestContext::class.java)?.run {
            rc.set(REQUEST_ACTION_METHOD, DEFAULT_ACTION_METHOD)
        } ?: kotlin.run {
            rc.forward(PathUtils.concat(view, requestUri))
            return
        }

        params.forEachIndexed { index, item -> rc.set("P${index + 1}", item) }

        val result: IResult<*> = action.execute(rc)
        result.execute(rc)
    }
}
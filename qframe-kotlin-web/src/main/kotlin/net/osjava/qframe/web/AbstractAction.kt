package net.osjava.qframe.web

import net.osjava.qframe.core.BeanFactory
import net.osjava.qframe.core.Constants.REQUEST_ACTION_METHOD
import net.osjava.qframe.utils.ClassUtils
import net.osjava.qframe.utils.JsonUtils
import org.slf4j.LoggerFactory
import java.lang.Exception

interface AbstractAction {
    private companion object {
        private val log = LoggerFactory.getLogger(AbstractAction::class.java)
    }

    fun execute(rc: RequestContext): IResult<*> {
        val methodName = rc.get(REQUEST_ACTION_METHOD) ?: "index"
        log.debug("Execute Action: class=${this::class.java.simpleName}, method=$methodName")

        val method = ClassUtils.getMethod(this::class.java, methodName, RequestContext::class.java)

        if (method == null) {
            rc.errorNotFound()
            return IResult.nothing()
        }

        val apiSecurity = method.getAnnotation(ApiSecurity::class.java)
        if (apiSecurity != null) {
            val apiBean = BeanFactory.getBean(apiSecurity.bean, IApiSecurity::class) ?: kotlin.run {
                rc.errorForbidden()
                return IResult.nothing()
            }
            val result = apiBean.validate(rc, apiSecurity)
            if (result !is ResultNothing) {
                return result
            }
        }

        return try {
            log.debug(">>> Request action {}: {}", rc.getRequestURI(), rc)
            (method.invoke(this, rc) as IResult<*>).also {
                log.debug("<<< Response from action ${rc.getRequestURI()}: ${it.data?.run { JsonUtils.toJson(this) }}")
            }
        } catch (e: Exception) {
            throw e.cause!!
        }
    }
}
package net.osjava.qframe.web

import com.google.gson.JsonElement
import net.osjava.qframe.core.Constants.CTX_TEMPLATE_EXT
import net.osjava.qframe.core.Constants.CTX_VIEW_SERVLET
import net.osjava.qframe.utils.JsonUtils
import net.osjava.qframe.utils.PathUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Controller
import java.lang.RuntimeException
import java.net.URLEncoder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.reflect

abstract class IResult<T>(val data: T) {
    internal abstract fun execute(rc: RequestContext)

    companion object {
        fun notion(): IResult<Any> = ResultNoting()

        fun render(action: KFunction<IResult<ViewPath>>): IResult<ViewPath> = ResultAction(action)

        fun render(jsonElement: JsonElement): IResult<JsonElement> = ResultJson(jsonElement)

        fun render(value: String): IResult<String> = ResultString(value)

        fun render(viewPath: ViewPath): IResult<ViewPath> = ResultView(viewPath)
    }
}

internal class ResultAction(private val action: KFunction<IResult<ViewPath>>) : IResult<ViewPath>(ViewPath("")) {
    override fun execute(rc: RequestContext) {
        val methodName = action.name
        val actionPath = action.javaMethod?.declaringClass?.getAnnotation(Controller::class.java)?.value
            ?: throw RuntimeException("The method[$methodName] is not in an Action")

        rc.redirect(PathUtils.concat(rc.getContextPath(), actionPath, methodName))
    }
}

internal class ResultNoting : IResult<Any>(Any()) {
    override fun execute(rc: RequestContext) {}
}

internal class ResultJson(jsonElement: JsonElement) : IResult<JsonElement>(jsonElement) {
    override fun execute(rc: RequestContext) {
        rc.response.contentType = "application/json"
        rc.response.writer.use { it.println(this.data.toString()) }
    }
}

internal class ResultString(value: String) : IResult<String>(value) {
    override fun execute(rc: RequestContext) {
        rc.response.contentType = "plain/text"
        rc.response.writer.use { it.println(this.data) }
    }
}

internal class ResultView(private val viewPath: ViewPath) : IResult<ViewPath>(viewPath) {
    override fun execute(rc: RequestContext) {
        when {
            viewPath.url.startsWith("http", true) || viewPath.redirect -> {
                val query = mutableListOf<String>()
                viewPath.forEach {
                    query.add("${it.key}=${URLEncoder.encode(it.value.toString(), "UTF-8")}")
                }
                val toUrl = StringBuilder().apply {
                    if (viewPath.redirect) this.append(rc.getContextPath())
                    val templateExt = rc.context.getAttribute(CTX_TEMPLATE_EXT) as String
                    this.append(viewPath.url.removeSuffix(templateExt))

                    if (query.isNotEmpty()) {
                        this.append("?").append(StringUtils.join(query, "&"))
                    }
                }

                rc.redirect(toUrl.toString())
                return
            }
            else -> {
                viewPath.forEach {
                    rc.setAttribute(it.key, it.value)
                }
                val viewServlet = rc.context.getAttribute(CTX_VIEW_SERVLET) as String
                rc.forward(PathUtils.concat(viewServlet, viewPath.url))
            }
        }
    }
}
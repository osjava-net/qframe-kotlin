package net.osjava.qframe.web.pebble


import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate
import net.osjava.qframe.utils.PathUtils
import net.osjava.qframe.web.RequestContext
import org.apache.commons.lang3.StringUtils

class PathFunction: Function {
    companion object {
        internal const val FUN_PATH_NAME = "path"
        private const val ARG_URL = "url"
        private const val ARG_PARAMS = "params"
    }

    override fun getArgumentNames(): MutableList<String> {
        return mutableListOf(ARG_URL, ARG_PARAMS)
    }

    override fun execute(p0: MutableMap<String, Any>, p1: PebbleTemplate, p2: EvaluationContext, p3: Int): Any {
        var url = p0[ARG_URL] as String

        (p0[ARG_PARAMS] as Map<*, *>?)?.let {
            val params = mutableListOf<String>()
            it.forEach { item-> params.add("${item.key.toString()}=${item.value.toString()}")}
            if (params.isNotEmpty()) {
                url = url + "?" + StringUtils.join(params,"&")
            }
        }

        return PathUtils.concat(RequestContext.get().getContextPath(), url)
    }
}
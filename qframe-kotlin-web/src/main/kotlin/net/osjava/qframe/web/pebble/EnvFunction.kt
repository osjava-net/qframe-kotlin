package net.osjava.qframe.web.pebble

import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate
import net.osjava.qframe.web.RequestContext
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class EnvFunction : Function {
    companion object {
        internal const val FunName = "env"
        private const val ArgKey = "key"
        private val logger = LoggerFactory.getLogger(EnvFunction::class.java)
    }

    override fun getArgumentNames(): MutableList<String> = mutableListOf(ArgKey)

    override fun execute(
        args: MutableMap<String, Any>,
        self: PebbleTemplate,
        context: EvaluationContext,
        lineNumber: Int
    ): Any {
        logger.debug("Get a variable in system environment: params={}", args)
        val key = args[ArgKey] as String
        logger.debug("Get a variable in system environment: key={}", key)

        val rc = RequestContext.get()

        val value = rc.getAttribute<String>(key)
            ?: rc.context.getAttribute(key)
            ?: System.getProperty(key)
            ?: System.getenv(key)
            ?: StringUtils.EMPTY

        logger.debug("Get a variable in system environment: key={}, value={}", key, value)
        return value
    }
}
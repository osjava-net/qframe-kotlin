package net.osjava.qframe.web

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.ServletLoader
import net.osjava.qframe.core.AppConfig
import net.osjava.qframe.core.Constants.CTX_TEMPLATE_EXT
import net.osjava.qframe.core.Constants.DEFAULT_TEMPLATE_NAME
import net.osjava.qframe.utils.PathUtils
import net.osjava.qframe.utils.PathUtils.concat
import net.osjava.qframe.utils.PathUtils.isFile
import net.osjava.qframe.web.pebble.WebExtension
import org.slf4j.LoggerFactory
import javax.servlet.ServletConfig
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class ViewServlet : HttpServlet() {
    companion object {
        private val log = LoggerFactory.getLogger(ViewServlet::class.java)

        private var templateExt = ".html.twig"
        private var templatePrefix = "/WEB-INF/templates"
        private var templateTheme = "/default"

        private val cacheViews = mutableListOf<String>()

        lateinit var pebbleEngine: PebbleEngine
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        this.doService(req)
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        this.doService(req)
    }

    override fun init(config: ServletConfig) {
        super.init(config)

        config.servletContext.getInitParameter("ext")?.let {
            templateExt = when (it.startsWith(".")) {
                true -> it
                false -> ".$it"
            }
        }

        config.servletContext.getInitParameter("prefix-template")?.let { templatePrefix = it }
        config.servletContext.getInitParameter("default")?.let { templateTheme = it }

        val builder = PebbleEngine.Builder()
        if (AppConfig.appEnv == AppConfig.AppEnv.DEVELOP) {
            builder.cacheActive(false)
        }
        builder.loader(ServletLoader(config.servletContext).apply {
            prefix = PathUtils.concat(templatePrefix, templateTheme)
        })
        builder.extension(WebExtension())

        pebbleEngine = builder.build()

        log.debug("Init ViewServlet: prefix=$templatePrefix, theme=$templateTheme, ext=$templateExt")
        config.servletContext.setAttribute(CTX_TEMPLATE_EXT, templateExt)
    }

    private fun doService(req: HttpServletRequest) {
        log.debug("Running ViewServlet: uri=${req.requestURI}, servlet=${req.servletPath}")

        val rc = RequestContext.get()
        val uri = req.requestURI.substringAfter(req.servletPath).removeSuffix(templateExt)

        val tpl = findTemplate(uri).ifEmpty {
            log.debug("Not found the template: $uri")
            rc.errorNotFound();
            return
        }

        uri.substringAfter(tpl).split("/").forEachIndexed { index, item -> rc.set("P${index + 1}", item) }

        render(tpl + templateExt)
    }

    private fun findTemplate(uri: String): String {
        if (uri.isEmpty()) return uri

        if (hasRealFile(concat(uri, DEFAULT_TEMPLATE_NAME))) return concat(uri, DEFAULT_TEMPLATE_NAME)

        if (hasRealFile(uri)) return uri

        return findTemplate(uri.substringBeforeLast("/"))
    }

    private fun hasRealFile(path: String): Boolean {
        log.debug("Check a template path is exists: {}", path)
        if (path.isEmpty()) return false

        if (cacheViews.contains(path)) return true

        val rc = RequestContext.get()
        val realPath = rc.context.getRealPath(concat(templatePrefix, templateTheme, path) + templateExt)

        log.debug("The real path of the template is: {}", realPath)
        if (isFile(realPath)) {
            cacheViews.add(path)
            return true;
        }

        return false
    }

    protected open fun render(url: String) {
        val rc = RequestContext.get()

        val context = mutableMapOf<String, Any>()
        rc.request.attributeNames.iterator().forEachRemaining {
            rc.getAttribute<Any>(it)?.let { value-> context[it]=value }
        }

        pebbleEngine.getTemplate(url.removePrefix("/")).evaluate(rc.response.writer!!, context)
    }
}
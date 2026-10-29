package net.osjava.qframe.web

import net.osjava.qframe.core.AppConfig
import net.osjava.qframe.core.Constants.DEFAULT_CHARSET
import net.osjava.qframe.utils.PathUtils
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.util.Streams
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

class RequestContext(var request: HttpServletRequest, var response: HttpServletResponse, var context: ServletContext) {
    private val parameters = mutableMapOf<String, String>()
    private val files = mutableMapOf<String, String>()

    fun get(key: String): String? {
        return parameters[key]?.takeIf { it.isNotEmpty() }
    }

    fun get(key: String, defValue: String): String {
        return get(key) ?: defValue
    }

    fun get(key: String, defValue: Int): Int {
        return get(key)?.toInt() ?: defValue
    }

    fun get(key: String, defValue: Long): Long {
        return get(key)?.toLong() ?: defValue
    }

    fun get(key: String, defValue: Boolean): Boolean {
        return get(key)?.toBoolean() ?: defValue
    }

    fun set(key: String, value: String) {
        parameters[key] = value
    }

    fun getSession(): HttpSession {
        return request.getSession(true)
    }

    inline fun <reified T : Any> getSession(key: String): T? {
        return getSession().getAttribute(key) as? T
    }

    fun setSession(key: String, value: Any) {
        getSession().setAttribute(key, value)
    }

    fun removeSession(key: String) {
        getSession().removeAttribute(key)
    }

    inline fun <reified T : Any> getAttribute(key: String): T? {
        return request.getAttribute(key) as? T
    }

    internal fun setFile(key: String, value: String) {
        this.parameters[key] = value
        this.files[key] = value
    }

    fun setAttribute(key: String, value: Any) {
        request.setAttribute(key, value)
    }

    fun forward(uri: String) {
        log.debug("Forward request to $uri")
        request.getRequestDispatcher(uri).forward(request, response)
    }

    fun redirect(url: String) {
        log.debug("Redirect request to $url")
        response.sendRedirect(url)
    }

    fun sendError(code: Int, message: String = "") {
        if (message.isEmpty()) {
            response.sendError(code)
        } else {
            response.sendError(code, message)
        }
    }

    fun errorNotFound() {
        sendError(HttpServletResponse.SC_NOT_FOUND)
    }

    fun errorForbidden() {
        sendError(HttpServletResponse.SC_FORBIDDEN)
    }

    fun errorException(message: String) {
        sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message)
    }

    fun closeCache() {
        setHeader("Pragma", "No-cache")
        setHeader("Cache-Control", "no-cache")
        setDateHeader("Expires", 0L)
    }

    fun getHeader(key: String?): String? {
        return request.getHeader(key)
    }

    fun setHeader(key: String, value: String) {
        response.setHeader(key, value)
    }

    fun getDateHeader(key: String): Long {
        return request.getDateHeader(key)
    }

    fun setDateHeader(key: String, value: Long) {
        response.setDateHeader(key, value)
    }

    fun getRemoteAddress(): String {
        val ip = getHeader("X-Forwarded-For") ?: getHeader("x-real-ip") ?: request.remoteAddr

        if (!ip.contains(".")) {
            return "127.0.0.1"
        }

        val ips = ip.split(",")
        for (item in ips) {
            if (item.isEmpty()) continue

            if (isIPAddr(item) && !item.startsWith("10.") && item.startsWith("192.168.") && item != "127.0.0.1") {
                return item.trim()
            }
        }

        return "127.0.0.1"
    }

    fun getContextPath(): String {
        return request.contextPath
    }

    fun getRequestURI(): String {
        return request.requestURI
    }

    fun getServletPath(): String {
        return request.servletPath
    }

    fun getAppUri(): String {
        return getRequestURI().substringAfter(getServletPath())
    }

    override fun toString(): String {
        return "RequestContext(${parameters})"
    }

    internal fun destroy() {
        cache.get()?.run {
            this.files.forEach {
                PathUtils.remove(it.value)
            }
            this.parameters.clear()
        }
    }

    companion object {
        private val cache = ThreadLocal<RequestContext>()
        private val log = LoggerFactory.getLogger(RequestContext::class.java)

        fun get(): RequestContext = cache.get()

        internal fun create(request: HttpServletRequest, response: HttpServletResponse,
                            context: ServletContext): RequestContext {
            val rc = cache.get()?.apply {
                this.request = request
                this.response = response
                this.context = context
            } ?: RequestContext(request, response, context).also { log.debug("Create an instance of RequestContext") }

            rc.request.characterEncoding = DEFAULT_CHARSET
            rc.response.characterEncoding = DEFAULT_CHARSET

            rc.response.setHeader("P3P",
                                  "CP='CURa ADMa DEVa PSAo PSDo OUR BUS UNI PUR INT DEM STA PRE COM NAV OTC NOI DSP COR'")
            if (AppConfig.get("cross.access.enable", false)) {
                rc.response.setHeader("Access-Control-Allow-Origin", "*")
                rc.response.setHeader("Access-Control-Allow-Headers",
                                      "access-control-allow-origin,X-Requested-With,x-qframe-certificate,Content-Type")
                rc.response.setHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS")
            }

            if (ServletFileUpload.isMultipartContent(request)) {
                parseMultipartRequest(request).forEach {
                    rc.setFile(it.key, it.value)
                }
            }

            parseRequest(request).forEach {
                rc.set(it.key, it.value)
            }

            cache.set(rc)
            return rc
        }

        private fun parseMultipartRequest(request: HttpServletRequest): MutableMap<String, String> {
            val params = mutableMapOf<String, String>()

            val fileIterator = ServletFileUpload().getItemIterator(request)
            while (fileIterator.hasNext()) {
                val item = fileIterator.next()
                if (item.isFormField) {
                    params[item.name] = Streams.asString(item.openStream())
                    continue
                }

                val fileName = File(item.name).name
                if (PathUtils.isDirectory(fileName)) continue

                val tempFileName = String.format("%d-%s.%s", System.currentTimeMillis(),
                                                 RandomStringUtils.random(12),
                                                 fileName.substringAfterLast("."))
                val targetFile = File(PathUtils.tempPath(LocalDate.now().format(DateTimeFormatter.ISO_DATE)),
                                      tempFileName)

                val source = BufferedInputStream(FileInputStream(fileName))
                val target = BufferedOutputStream(FileOutputStream(targetFile))
                Streams.copy(source, target, true)

                params[item.fieldName] = targetFile.absolutePath
            }

            return params
        }

        private fun parseRequest(request: HttpServletRequest): MutableMap<String, String> {
            val params = mutableMapOf<String, String>()

            request.parameterNames.iterator().forEachRemaining {
                params[it] = request.getParameter(it)
            }

            return params
        }

        private fun isIPAddr(address: String): Boolean {
            if (StringUtils.isEmpty(address)) {
                return false
            }
            val ips = StringUtils.split(address, '.')
            if (ips.size != 4) {
                return false
            }
            return try {
                val ipa = ips[0].toInt()
                val ipb = ips[1].toInt()
                val ipc = ips[2].toInt()
                val ipd = ips[3].toInt()
                ipa in 0..255 && ipb >= 0 && ipb <= 255 && ipc >= 0 && ipc <= 255 && ipd >= 0 && ipd <= 255
            } catch (e: Exception) { // Ignore Exception; if an exception, this method would return FALSE.
                false
            }
        }
    }
}

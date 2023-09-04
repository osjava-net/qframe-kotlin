package net.osjava.qframe.utils

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.osjava.qframe.core.AppConfig
import net.osjava.qframe.utils.JsonUtils.fromJson
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus
import org.apache.http.NameValuePair
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.utils.URIBuilder
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import java.io.IOException

object HttpUtils {
    private const val ENCODE_UTF8 = "UTF-8"
    private const val DEFAULT_TIMEOUT = 5000
    private val log = LoggerFactory.getLogger(javaClass)

    private lateinit var httpClient: CloseableHttpClient

    internal fun initialize() {
        val pm = PoolingHttpClientConnectionManager().apply {
            this.maxTotal = AppConfig.get("http.client.max.total", 100)
            this.defaultMaxPerRoute = AppConfig.get("http.client.max.per.route", 20)
        }

        httpClient = HttpClients.custom().setConnectionManager(pm).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build()
        log.debug("Initialize HttpUtils: maxTotal=${pm.maxTotal}, maxPerRoute=${pm.defaultMaxPerRoute}")
    }

    internal fun destroy() {
        if (this::httpClient.isInitialized) {
            httpClient.close()
        }
    }

    fun postStream(url: String, content: JsonElement, timeout: Int = DEFAULT_TIMEOUT,
                   headers: List<Header> = emptyList()): Response<JsonElement> {
        val response: Response<String> = postStream(url, content.toString(), ContentType.APPLICATION_JSON, timeout,
                                                    headers)
        return Response<JsonElement>(response.statusCode).apply {
            if (response.isSuccess()) {
                this.content = fromJson(response.content ?: "", JsonElement::class)
            } else {
                this.content = JsonPrimitive(response.content)
            }
        }
    }

    fun postStream(url: String, content: String, contentType: ContentType = ContentType.APPLICATION_XHTML_XML,
                   timeout: Int = DEFAULT_TIMEOUT, headers: List<Header> = emptyList()): Response<String> {
        log.debug(
            ">>> Request remote url by post stream: url={}, content={}, contentType={}, timeout={}, headers={}",
            url,
            content,
            contentType,
            timeout,
            headers
        )

        val httpPost = HttpPost(url).apply {
            this.config = getRequestConfig(timeout)
            this.entity = StringEntity(content, contentType.withCharset("UTF-8"))
            headers.forEach { this.setHeader(it.name, it.value) }
        }

        return doMethod(httpPost).also {
            log.debug("<<< Request from url<{}>: {}", url, it)
        }
    }

    fun postForm(url: String, timeout: Int, headers: List<Header>, vararg parameters: Parameter): Response<String> {
        log.debug(
            ">>> Request url by form: url={}, parameters={}, timeout={}, headers={}",
            url,
            parameters,
            timeout,
            headers
        )

        val params = mutableListOf<NameValuePair>()
        for ((name, value) in parameters) {
            params.add(BasicNameValuePair(name, value))
        }

        val httpPost = HttpPost(url).apply {
            this.config = getRequestConfig(timeout)
            this.entity = UrlEncodedFormEntity(params, "UTF-8")
            headers.forEach { this.addHeader(it.name, it.value) }
        }

        return doMethod(httpPost).also {
            log.debug("<<< Request from url<{}>: {}", url, it)
        }
    }

    fun request(url: String, timeout: Int = DEFAULT_TIMEOUT, headers: List<Header> = emptyList(),
                vararg parameters: Parameter = emptyArray()): Response<String> {
        log.debug(">>> Request url: url={}, parameters={}, timeout={}, headers={}", url, parameters, timeout, headers)

        val uriBuilder = URIBuilder(url).apply {
            parameters.forEach { this.addParameter(it.name, it.value) }
        }

        val httpGet = HttpGet(uriBuilder.build()).apply {
            this.config = getRequestConfig(timeout)
            headers.forEach { this.addHeader(it.name, it.value) }
        }

        return doMethod(httpGet).also {
            log.debug("<<< Response from url<{}>: {}", url, it)
        }
    }

    private fun getRequestConfig(timeout: Int): RequestConfig? {
        return RequestConfig.custom().apply {
            this.setSocketTimeout(timeout)
            this.setConnectTimeout(timeout)
            this.setConnectionRequestTimeout(timeout)
        }.build()
    }

    private fun doMethod(httpMethod: HttpUriRequest): Response<String> {
        httpMethod.setHeader(HttpHeaders.CONNECTION, "Close")

        try {
            httpClient.execute(httpMethod).use { response ->
                return Response<String>(response.statusLine.statusCode).apply {
                    if (this.isSuccess()) this.content = EntityUtils.toString(response.entity)
                }
            }
        } catch (e: IOException) {
            return Response(HttpStatus.SC_BAD_GATEWAY, e.message)
        }
    }

    data class Header(val name: String, val value: String)

    data class Parameter(val name: String, val value: String)

    data class Response<T : Any>(val statusCode: Int, var content: T? = null) {
        fun isSuccess(): Boolean = statusCode == HttpStatus.SC_OK
    }
}
package net.osjava.qframe.core

import net.osjava.qframe.utils.PathUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.util.*

object AppConfig {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private val properties = Properties()

    lateinit var appId: String
    lateinit var workspace: String
    var appEnv = AppEnv.RELEASE

    internal fun initialize(appId: String, workspace: String, extraProperties: Properties? = null) {
        this.appId = appId
        this.workspace = workspace

        properties.load(FileInputStream(File(PathUtils.concat(workspace, "/conf/config.properties"))))

        extraProperties?.forEach {
            properties[it.key] = it.value
        }

        this.appEnv = get("app.env")?.let { AppConfig.AppEnv.valueOf(it.uppercase()) }!!

        if (log.isDebugEnabled) {
            val builder = StringBuilder(1024)
            properties.forEach {
                builder.append("|- ").append(it.key).append("=").append(it.value).append("\n")
            }

            log.debug("Initialize configuration:\n{}", builder.toString())
        }
    }

    internal fun properties() = this.properties

    fun get(key: String): String? {
        return (properties[key] as String?)?.takeIf { it.isNotEmpty() }
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

    enum class AppEnv {
        DEVELOP, TEST, RELEASE
    }
}
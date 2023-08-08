package net.osjava.qframe.utils

import net.osjava.qframe.core.AppConfig
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.File

object PathUtils {
    private val log = LoggerFactory.getLogger(PathUtils::class.java)

    fun concat(basePath: String, vararg subPaths: String): String {
        val builder = StringBuilder(1024)
        when (basePath.endsWith("/")) {
            true -> builder.append(basePath.substring(0, basePath.length - 1))
            false -> builder.append(basePath)
        }

        for (item in subPaths) {
            if (!item.startsWith("/")) builder.append("/")

            when (item.endsWith("/")) {
                true -> builder.append(item.substring(0, item.length - 1))
                false -> builder.append(item)
            }
        }

        return when (builder.isEmpty()) {
            true -> basePath
            false -> builder.toString()
        }
    }

    fun isFile(path: String): Boolean = File(path).isFile

    fun isDirectory(path: String): Boolean = File(path).isDirectory

    fun makeDirectory(path: String) {
        if (isFile(path) || isDirectory(path)) return

        File(path).run { this.mkdirs() }
    }

    fun remove(path: String) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            log.error("Delete file has an exception: ${e.message}")
        }
    }

    fun list(path: String): List<String> {
        if (isFile(path)) return listOf(StringUtils.substringAfterLast(path, "/"))

        File(path).list()?.let {
            return listOf(*it)
        } ?: return listOf()
    }

    fun tempPath(path: String): String {
        val realPath = concat(AppConfig.workspace, "/temp", path)
        makeDirectory(realPath)
        return realPath
    }
}
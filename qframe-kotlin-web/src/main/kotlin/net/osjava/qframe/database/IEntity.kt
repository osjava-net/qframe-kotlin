package net.osjava.qframe.database

import com.google.gson.JsonElement
import jakarta.persistence.Id
import net.osjava.qframe.utils.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serializable
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

interface IEntity {
    fun uid() : Serializable {
        print(this::class.memberProperties.first().javaField?.getAnnotation(Id::class.java))
        val field = this::class.memberProperties.first{
            it.javaField?.getAnnotation(Id::class.java) != null
        }

        return field.getter.call(this) as Serializable
    }
    fun buildJson(): JsonElement {
        return JsonUtils.toJsonTree(this)
    }
}
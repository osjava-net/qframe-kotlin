package net.osjava.qframe.utils

import com.google.gson.*
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern
import kotlin.reflect.KClass

object JsonUtils {
    private const val DATE_TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss"

    fun toJsonTree(obj: Any, adapter: ((R:GsonBuilder) -> Unit)? = null): JsonElement {
        return getBuilder(adapter).create().toJsonTree(obj)
    }

    fun toJson(obj: Any, adapter: ((R: GsonBuilder) -> Unit)? = null): String {
        return getBuilder(adapter).create().toJson(obj)
    }

    fun <T: Any> fromJson(obj: String, clazz: KClass<T>, adapter: ((R: GsonBuilder) -> Unit)? = null): T {
        return getBuilder(adapter).create().fromJson(obj, clazz.java)
    }

    fun format(json: JsonElement, description: String? = null, adapter: ((R: GsonBuilder) -> Unit)? = null): String {
        val builder = StringBuilder(1024)
        if (description?.isNotEmpty() == true) {
            builder.append("=== BEGIN <$description> ===")
        }
        builder.append(getBuilder(adapter).setPrettyPrinting().create().toJson(json))
        if (description?.isNotEmpty() == true) {
            builder.append("=== END <$description> ===")
        }
        return builder.toString()
    }

    private fun getBuilder(adapter: ((R: GsonBuilder) -> Unit)?): GsonBuilder {
        val builder = GsonBuilder()

        builder.registerTypeAdapter(LocalDate::class.java,
                                    JsonSerializer<LocalDate> { src, _, _ ->
                                        JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE))
                                    })
        builder.registerTypeAdapter(LocalDate::class.java,
                                    JsonDeserializer { json, _, _ ->
                                        LocalDate.parse(json.toString())
                                    })

        builder.registerTypeAdapter(LocalDateTime::class.java,
                                    JsonSerializer<LocalDateTime> { src, _, _ ->
                                        JsonPrimitive(src.format(ofPattern(DATE_TIME_FORMATTER)))
                                    })
        builder.registerTypeAdapter(LocalDateTime::class.java,
                                    JsonDeserializer { json, _, _ ->
                                        LocalDateTime.parse(json.toString(), ofPattern(DATE_TIME_FORMATTER))
                                    })

        if (adapter != null) {
            adapter(builder)
        }
        return builder
    }
}
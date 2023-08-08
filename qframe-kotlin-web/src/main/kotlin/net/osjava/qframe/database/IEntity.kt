package net.osjava.qframe.database

import com.google.gson.JsonElement
import net.osjava.qframe.utils.JsonUtils

interface IEntity {
    fun buildJson(): JsonElement {
        return JsonUtils.toJsonTree(this)
    }
}
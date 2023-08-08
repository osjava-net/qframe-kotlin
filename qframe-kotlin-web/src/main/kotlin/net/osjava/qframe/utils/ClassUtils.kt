package net.osjava.qframe.utils

import java.lang.reflect.Method
import kotlin.reflect.KClass

object ClassUtils {
    fun getMethod(clazz: Class<*>, name: String, vararg parameters: Class<*>): Method? {
        if(name.isEmpty()) return null

        return try {
            clazz.getMethod(name, *parameters)
        } catch (e: NoSuchMethodException) {
            null
        }
    }
}
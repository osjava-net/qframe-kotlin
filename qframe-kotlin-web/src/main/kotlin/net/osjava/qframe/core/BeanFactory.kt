package net.osjava.qframe.core

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import kotlin.reflect.KClass

object BeanFactory {
    private lateinit var applicationContext: ApplicationContext

    internal fun initialize(contexts: List<String>) {
        applicationContext = ClassPathXmlApplicationContext(*contexts.toTypedArray())
    }

    fun <T : Any> getBean(clazz: KClass<out T>): T? {
        return applicationContext.getBean(clazz.java)
    }

    fun <T : Any> getBean(name: String, clazz: KClass<out T>): T? {
        return applicationContext.getBean(name, clazz.java)
    }

    fun getBeansWithAnnotation(clazz: KClass<out Annotation>): MutableMap<String, Any> {
        return applicationContext.getBeansWithAnnotation(clazz.java)
    }

    fun containsBean(name: String): Boolean {
        return applicationContext.containsBean(name)
    }
}
package net.osjava.qframe.core

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class EventService {
    private companion object {
        private val LISTENERS = mutableMapOf<KClass<out Event<*>>, MutableList<kotlin.Any>>()
    }

    @Synchronized
    fun <T : Any> registerListener(clazz: KClass<out Event<T>>, listener: (Event<T>) -> Unit) {
        val listListener = LISTENERS.computeIfAbsent(clazz) { mutableListOf() }
        listListener.add(listener)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> executeEvent(event: Event<T>) {
        LISTENERS[event::class]?.forEach {
            val handler = it as (Event<T>) -> Unit
            handler(event)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> dispatchEvent(event: Event<T>) {
        LISTENERS[event::class]?.forEach {
            val handler = it as (Event<T>)->Unit
            GlobalScope.launch { handler(event) }
        }
    }
}
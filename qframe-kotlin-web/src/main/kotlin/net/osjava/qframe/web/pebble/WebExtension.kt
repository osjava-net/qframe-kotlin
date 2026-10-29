package net.osjava.qframe.web.pebble

import io.pebbletemplates.pebble.extension.AbstractExtension
import io.pebbletemplates.pebble.extension.Function
import net.osjava.qframe.web.pebble.PathFunction.Companion.FUN_PATH_NAME

class WebExtension : AbstractExtension() {
    override fun getFunctions(): Map<String, Function> {
        val functions = mutableMapOf<String, Function>()
        functions[FUN_PATH_NAME] = PathFunction()
        functions[EnvFunction.FunName] = EnvFunction()

        return functions
    }
}
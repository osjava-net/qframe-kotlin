package net.osjava.qframe.web

class ViewPath(val url: String, val redirect: Boolean = false) {
    private val parameters = mutableMapOf<String, Any>()

    fun with(key: String, value: Any): ViewPath {
        parameters[key] = value
        return this
    }

    fun forEach(iterator: (Map.Entry<String, Any>) -> Unit) {
        parameters.forEach(iterator)
    }

    override fun toString(): String {
        return "ViewPath(url=$url, parameters=$parameters)"
    }
}
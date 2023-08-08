package net.osjava.qframe.database

import kotlin.math.ceil

class Page<T>(val number: Int = 1, val size: Int = DEFAULT_PAGE_SIZE, private val total: Long = 0, private val data: List<T> = emptyList()) {
    companion object {
        private const val FIRST_PAGE = 1;
        const val DEFAULT_PAGE_SIZE = 200;
        fun <T> default(): Page<T> = Page()
        fun <T> size(size:Int): Page<T> = Page(size = size)
    }

    val offset: Int = if (number <= 0) 0 else (number - 1) * size
    val first: Int = if (number <= 0) 0 else FIRST_PAGE
    val last = when (size) {
        0, 1 -> 1
        else -> ceil(total * 1.0 / size).toInt()
    }

    val next: Int = if (number < last) number + 1 else last
    val previous: Int = if (number > FIRST_PAGE) number - 1 else FIRST_PAGE
    val hasNext: Boolean = number < last
    val hasPrevious: Boolean = number > FIRST_PAGE
}
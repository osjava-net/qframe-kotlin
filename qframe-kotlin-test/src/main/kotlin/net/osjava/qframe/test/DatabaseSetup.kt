package net.osjava.qframe.test

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DatabaseSetup(val value: String, val dataSource: String = "dataSource", val schema: String = "")
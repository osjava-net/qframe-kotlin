package net.osjava.qframe.web

annotation class ApiSecurity(
    val bean: String = "apiSecurity",
    val names: Array<String> = []
)
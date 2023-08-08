package net.osjava.qframe.database

import org.hibernate.SessionFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Repository

@Repository
@Scope("prototype")
class Database(open val sessionFactory: SessionFactory) {
    private lateinit var resultClass: Class<Any>
    private lateinit var rootClass: Class<Any>

    fun query(resultClass: Class<Any>, rootClass: Class<Any>? = null): Database {
        this.resultClass = resultClass
        this.rootClass = rootClass ?: resultClass
        return this
    }

    fun where(column: String, value: Any, operation: String = "=", condition: String = "and") {

    }
}
package net.osjava.qframe.test

import net.osjava.qframe.core.BeanFactory
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.dbunit.ext.mysql.MySqlConnection
import org.dbunit.operation.DatabaseOperation
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.test.context.TestContext
import org.springframework.test.context.support.AbstractTestExecutionListener
import java.io.FileNotFoundException
import java.util.*
import javax.sql.DataSource

class DatabaseTestExecutionListener : AbstractTestExecutionListener() {
    override fun beforeTestClass(testContext: TestContext) {
        val databaseSetup: DatabaseSetup = testContext.testClass.getAnnotation(DatabaseSetup::class.java) ?: return

        initDatabase(databaseSetup)
    }

    override fun afterTestClass(testContext: TestContext) {
        val databaseSetup: DatabaseSetup = testContext.testClass.getAnnotation(DatabaseSetup::class.java) ?: return
        cleanDatabase(databaseSetup)
    }

    private fun initDatabase(databaseSetup: DatabaseSetup) {
        val ds = BeanFactory.getBean(databaseSetup.dataSource, DataSource::class) ?: return

        val conn: IDatabaseConnection = getConnection(ds, databaseSetup.schema)

        try {
            val dataResource: Resource = resourceLoader.getResource(databaseSetup.value)
            if (!dataResource.exists()) {
                throw FileNotFoundException("Data init file [${databaseSetup.value}] not exist.")
            }

            FlatXmlDataSetBuilder().setColumnSensing(true).build(dataResource.inputStream).let {
                val rDataSet = ReplacementDataSet(it)
                rDataSet.addReplacementObject("[now]", Date())
                DatabaseOperation.CLEAN_INSERT.execute(conn, rDataSet)
            }
        } finally {
            conn.close()
        }
    }

    private fun cleanDatabase(databaseSetup: DatabaseSetup) {
        val ds = BeanFactory.getBean(databaseSetup.dataSource, DataSource::class) ?: return

        val conn: IDatabaseConnection = getConnection(ds, databaseSetup.schema)
        try {
            val dataResource = resourceLoader.getResource(databaseSetup.value)
            if (!dataResource.exists()) {
                throw FileNotFoundException("Data init file [${databaseSetup.value}] not exist.")
            }

            FlatXmlDataSetBuilder().setColumnSensing(true).build(dataResource.inputStream).let {
                DatabaseOperation.DELETE_ALL.execute(conn, ReplacementDataSet(it))
            }
        } finally {
            conn.close()
        }
    }
    
    private fun getConnection(dataSource: DataSource, schema: String): IDatabaseConnection {
        val mysqlConn = dataSource.connection.apply { this.autoCommit = true }
        return MySqlConnection(mysqlConn, schema)
    }

    companion object {
        private val resourceLoader: ResourceLoader = DefaultResourceLoader()
    }
}
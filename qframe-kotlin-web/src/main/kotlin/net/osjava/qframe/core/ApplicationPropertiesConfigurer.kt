package net.osjava.qframe.core

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.*
import org.springframework.lang.Nullable
import org.springframework.util.StringValueResolver
import java.util.*

//private const val LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME = "localProperties"
//private const val ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME = "environmentProperties"

class ApplicationPropertiesConfigurer : PlaceholderConfigurerSupport(), EnvironmentAware {

    private val LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME = "localProperties"
    private val ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME = "environmentProperties"

    private lateinit var environment: Environment

    override fun processProperties(beanFactory: ConfigurableListableBeanFactory, props: Properties) {
        val propertySources = MutablePropertySources()

        propertySources.addLast(
                object : PropertySource<Environment?>(ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME, environment) {
                    @Nullable
                    override fun getProperty(key: String): String? {
                        return this.source!!.getProperty(key)
                    }
                }
        )

        AppConfig.properties().forEach { t, u -> props[t] = u }

        val localPropertySource: PropertySource<*> = PropertiesPropertySource(LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME, props)
        propertySources.addFirst(localPropertySource)

        val propertyResolver = PropertySourcesPropertyResolver(propertySources)

        propertyResolver.setPlaceholderPrefix(placeholderPrefix)
        propertyResolver.setPlaceholderSuffix(placeholderSuffix)
        propertyResolver.setValueSeparator(valueSeparator)

        val valueResolver = StringValueResolver { strVal: String? ->
            var resolved = if (ignoreUnresolvablePlaceholders) propertyResolver.resolvePlaceholders(strVal) else propertyResolver.resolveRequiredPlaceholders(strVal)
            if (trimValues) {
                resolved = resolved.trim { it <= ' ' }
            }
            if (resolved == nullValue) null else resolved
        }

        doProcessProperties(beanFactory, valueResolver)
    }

    override fun setEnvironment(environment: Environment) {
        this.environment = environment;
    }
}
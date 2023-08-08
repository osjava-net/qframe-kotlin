package net.osjava.qframe.test

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(locations = ["classpath*:/META-INF/spring-beans.xml", "classpath*:/META-INF/spring-test.xml"])
@TestExecutionListeners(AppTestExecutionListener::class, DatabaseTestExecutionListener::class)
@ActiveProfiles("UnitTest")
abstract class SpringTest
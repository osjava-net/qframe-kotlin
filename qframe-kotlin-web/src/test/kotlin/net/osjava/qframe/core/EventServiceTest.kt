package net.osjava.qframe.core

import org.junit.jupiter.api.Test

internal class EventServiceTest {
    @Test
    internal fun `Fire event`() {
        val eventService = EventService()
        eventService.registerListener(EventTestData::class) {
            Thread.sleep(2000)
            println("On event A... ${it.data}")
        }

        eventService.registerListener(EventTestData::class) {
            Thread.sleep(1000)
            println("On event B...${it.data}")
        }

        eventService.dispatchEvent(EventTestData("123"))
        Thread.sleep(5000)
    }
}

class EventTestData(data: String) : Event<String>(data)
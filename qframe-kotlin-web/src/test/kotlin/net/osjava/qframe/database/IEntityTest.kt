package net.osjava.qframe.database

import jakarta.persistence.Id
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class IEntityTest {

    @Test fun test_get_uid() {
        val t = TestEntity(200L)
        assertEquals(200L, t.uid())
    }

    class TestEntity(
        @Id var uid: Long = 0L
    ) : IEntity
}
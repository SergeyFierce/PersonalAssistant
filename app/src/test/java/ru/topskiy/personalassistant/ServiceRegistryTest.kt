package ru.topskiy.personalassistant

import org.junit.Test
import ru.topskiy.personalassistant.core.model.ServiceId
import ru.topskiy.personalassistant.core.model.ServiceRegistry
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Unit-тесты реестра сервисов (без Android). */
class ServiceRegistryTest {

    @Test
    fun displayOrderContainsAllServiceIds() {
        val order = ServiceRegistry.displayOrder
        assertEquals(ServiceId.entries.size, order.size)
        assertEquals(ServiceId.entries.toSet(), order.toSet())
    }

    @Test
    fun allServicesMatchDisplayOrder() {
        val all = ServiceRegistry.all
        assertEquals(ServiceRegistry.displayOrder.size, all.size)
        ServiceRegistry.displayOrder.forEachIndexed { index, id ->
            assertEquals(id, all[index].id)
        }
    }

    @Test
    fun byIdReturnsServiceForEveryEnumValue() {
        ServiceId.entries.forEach { id ->
            val service = ServiceRegistry.byId(id)
            assertNotNull(service)
            assertEquals(id, service.id)
        }
    }

    @Test
    fun groupedByCategoryCoversAllServices() {
        val grouped = ServiceRegistry.groupedByCategory
        assertTrue(grouped.isNotEmpty())
        val allInGrouped = grouped.flatMap { it.second }.toSet()
        assertEquals(ServiceRegistry.all.toSet(), allInGrouped)
    }
}

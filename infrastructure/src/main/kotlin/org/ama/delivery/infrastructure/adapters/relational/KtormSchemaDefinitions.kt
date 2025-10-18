package org.ama.delivery.infrastructure.adapters.relational
import org.ama.delivery.core.domain.entities.OrderStatus
import org.ktorm.schema.*

/**
 * Определение таблицы 'couriers' для Ktorm.
 */
object CouriersTable : Table<Nothing>("couriers") {
    val id = uuid("id").primaryKey()
    val name = text("name")
    val speed = int("speed")
    val locationX = int("location_x")
    val locationY = int("location_y")
}

object OrdersTable : Table<Nothing>("orders") {
    val id = uuid("id").primaryKey()
    val destX = int("dest_x")
    val destY = int("dest_y")
    val volume = int("volume")
    val status = enum<OrderStatus>("status")
    val courierId = uuid("courier_id")
}

object StoragePlacesTable : Table<Nothing>("storage_places") {
    val id = uuid("id").primaryKey()
    val name = text("name")
    val maxVolume = int("total_volume")
    val orderId = uuid("order_id")
    val courierId = uuid("courier_id")
}
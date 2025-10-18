package org.ama.delivery.infrastructure.adapters.relational

import arrow.core.getOrElse
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.LocationError
import org.ama.delivery.core.domain.common.Volume
import org.ama.delivery.core.domain.common.VolumeError
import org.ama.delivery.core.domain.entities.CourierId
import org.ama.delivery.core.domain.entities.Order
import org.ama.delivery.core.domain.entities.OrderId
import org.ama.delivery.core.domain.entities.OrderStatus
import org.ama.delivery.core.ports.outbound.IOrderRepository

import org.ktorm.database.Database

import org.ktorm.dsl.QueryRowSet
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.dsl.insert
import org.ktorm.dsl.update

class KtormOrderRepository(private val database: Database) : IOrderRepository {
    override fun addNewOrder(order: Order) {
        if (KtormTransactionContext.get() == null)
            error("Operation should be run in transaction")

        database.insert(OrdersTable) {
            set(it.id, order.id().toUUID())
            set(it.volume, order.volume.toInt())
            set(it.destX, order.destination.xToInt())
            set(it.destY, order.destination.yToInt())
            set(it.status, order.status())
            set(it.courierId, order.courierId()?.toUUID())
        }
    }

    override fun updateOrder(order: Order) {
        if (KtormTransactionContext.get() == null)
            error("Operation should be run in transaction")

        database.update(OrdersTable) {
            set(it.volume, order.volume.toInt())
            set(it.destX, order.destination.xToInt())
            set(it.destY, order.destination.yToInt())
            set(it.status, order.status())
            set(it.courierId, order.courierId()?.toUUID())
            where { it.id eq order.id().toUUID() }
        }
    }

    override fun getOrderById(orderId: OrderId): Order? {
        return database.from(OrdersTable)
            .select()
            .where { OrdersTable.id eq orderId.toUUID() }
            .map { row -> row.toOrder() }
            .firstOrNull()
    }

    override fun getRandomCreatedOrder(): Order? {
        return database.from(OrdersTable)
            .select()
            .where { OrdersTable.status eq OrderStatus.Created }
            .map { row -> row.toOrder() }
            .firstOrNull()
    }

    override fun getAssignedOrders(): List<Order> {
        return database.from(OrdersTable)
            .select()
            .where { OrdersTable.status eq OrderStatus.Assigned }
            .map { row -> row.toOrder() }
    }

    private fun QueryRowSet.toOrder(): Order {
        val id = OrderId(this[OrdersTable.id]!!)
        val volume = Volume.from(this[OrdersTable.volume]!!)
            .getOrElse { error ->
                when (error) {
                    is VolumeError.IncorrectVolumeValue -> throw IllegalArgumentException("Incorrect volume value: ${error.value}")
                }
            }

        val destX = this[OrdersTable.destX]!!
        val destY = this[OrdersTable.destY]!!
        val destination = Location.from(destX, destY)
            .getOrElse { error ->
                when (error) {
                    is LocationError.IncorrectCoordinates -> throw IllegalArgumentException("Incorrect coordinates: (${error.x}, ${error.y})")
                }
            }

        val status: OrderStatus = this[OrdersTable.status]!!
        val courierId = this[OrdersTable.courierId]?.let { CourierId(it) }

        return Order.reconstitute(id, destination, volume, status, courierId)
    }
}
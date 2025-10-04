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

class KtormOrderRepository(private val database: Database) : IOrderRepository {
    override fun addNewOrder(order: Order) {
        if (KtormTransactionContext.get() == null)
            throw IllegalStateException("Operation should be run in transaction")
    }

    override fun updateOrder(order: Order) {
        if (KtormTransactionContext.get() == null)
            throw IllegalStateException("Operation should be run in transaction")
    }

    override fun getOrderById(orderId: OrderId): Order? {
        val foundOrder = database.from(OrdersTable)
            .select()
            .where { OrdersTable.id eq orderId.toUUID() }
            .map { row ->
                val id = OrderId(row[OrdersTable.id]!!)
                val volume = Volume.from(row[OrdersTable.volume]!!)
                    .getOrElse { error ->
                        when (error) {
                            is VolumeError.IncorrectVolumeValue -> throw IllegalArgumentException("Incorrect volume value: ${error.value}")
                        }
                    }

                val destX = row[OrdersTable.destX]!!
                val destY = row[OrdersTable.destY]!!
                val destination = Location.from(destX, destY)
                    .getOrElse { error ->
                        when (error) {
                            is LocationError.IncorrectCoordinates -> throw IllegalArgumentException("Incorrect coordinates: (${error.x}, ${error.y})")
                        }
                    }

                val status: OrderStatus = row[OrdersTable.status]!!
                val courierId = CourierId(row[OrdersTable.courierId]!!)

                Order.reconstitute(id, destination, volume, status, courierId)
            }
            .firstOrNull()

        return foundOrder
    }

    override fun getRandomCreatedOrder(): Order? {
        TODO("Not yet implemented")
    }

    override fun getAssignedOrders(): List<Order> {
        TODO("Not yet implemented")
    }

    private fun QueryRowSet.toOrder(): Order {
        TODO("Not yet implemented")
    }
}
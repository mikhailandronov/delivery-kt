package org.ama.delivery.infrastructure.adapters.relational

import org.ama.delivery.core.domain.entities.Order
import org.ama.delivery.core.domain.entities.OrderId
import org.ama.delivery.core.ports.outbound.IOrderRepository

import org.ktorm.database.Database

import org.ktorm.dsl.QueryRowSet
import org.ktorm.dsl.eq
import org.ktorm.dsl.forEach
import org.ktorm.dsl.from
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
        database.from(OrdersTable)
            .select()
            .where { OrdersTable.id eq orderId.toUUID() }
            .forEach { raw ->
                println("${raw[OrdersTable.id]}: ${raw[OrdersTable.status]}")
            }


        return null
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
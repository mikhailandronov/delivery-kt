package org.ama.delivery.core.ports.outbound

import org.ama.delivery.core.domain.entities.Order
import org.ama.delivery.core.domain.entities.OrderId

interface IOrderRepository {
    fun addNewOrder(order: Order)
    fun updateOrder(order: Order)
    fun getOrderById(orderId: OrderId): Order?
    fun getRandomCreatedOrder(): Order?
    fun getAssignedOrders(): List<Order>
}
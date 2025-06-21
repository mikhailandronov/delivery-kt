package org.ama.delivery.core.domain.entities

import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ama.delivery.core.domain.common.AbstractUuidId
import org.ama.delivery.core.domain.common.AggregateRoot
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.Volume
import java.util.UUID

class OrderId(value: UUID = UUID.randomUUID()) : AbstractUuidId(value)

sealed class OrderError {
    data class IncorrectVolume(val volume: Volume) : OrderError()
}

class Order
private constructor(
    private val id: OrderId,
    val destination: Location,
    val volume: Volume,
    private var status: OrderStatus = OrderStatus.Created,
    private var courierId: CourierId? = null
) : AggregateRoot<OrderId> {

    override fun id() = id
    fun status() = status
    fun courierId() = courierId

    companion object {
        fun create(id: OrderId, destination: Location, volume: Volume) = either<OrderError, Order> {
            ensure(volume.toInt() > 0){
                OrderError.IncorrectVolume(volume)
            }
            val order = Order(id, destination, volume)
            order
        }
    }

    fun assign(courier: Courier) = either<OrderError, Unit>{

    }

    fun complete()= either<OrderError, Unit>{

    }
}
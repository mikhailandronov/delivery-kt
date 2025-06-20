package org.ama.delivery.core.domain.entities

import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ama.delivery.core.domain.common.AbstractUuidId
import org.ama.delivery.core.domain.common.AggregateRoot
import org.ama.delivery.core.domain.common.Location
import java.util.UUID

class OrderId(value: UUID = UUID.randomUUID()) : AbstractUuidId(value)

sealed class OrderError {
    data class IncorrectVolume(val volume: Int) : OrderError()
}

class Order
private constructor(
    private val id: OrderId,
    val location: Location,
    val volume: Int
) : AggregateRoot<OrderId> {

    override fun id() = id

    companion object {

        fun create(location: Location, volume: Int)
        = reconstitute(OrderId(), location, volume)

        internal fun reconstitute(id: OrderId, location: Location, volume: Int) = either<OrderError, Order> {
            ensure(volume > 0){
                OrderError.IncorrectVolume(volume)
            }
            val order = Order(id, location, volume)
            order
        }
    }
}
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
    data class CantAssignInStatus(val status: OrderStatus) : OrderError()
    data class CantCompleteInStatus(val status: OrderStatus) : OrderError()
}

class Order
private constructor(
    private val id: OrderId,
    val destination: Location,
    val volume: Volume,
    private var status: OrderStatus,
    private var courierId: CourierId?
) : AggregateRoot<OrderId> {

    override fun id() = id
    fun status() = status
    fun courierId() = courierId

    companion object {
        fun create(destination: Location, volume: Volume) = either<OrderError, Order> {
            ensure(volume > Volume.zeroVolume()) {
                OrderError.IncorrectVolume(volume)
            }
            reconstitute(OrderId(), destination, volume)
        }

        fun reconstitute(
            id: OrderId,
            destination: Location,
            volume: Volume,
            status: OrderStatus = OrderStatus.Created,
            courierId: CourierId? = null
        ) =
            Order(id, destination, volume, status, courierId)
    }

    fun assign(courier: Courier) = either<OrderError, Unit> {
        ensure(status() == OrderStatus.Created) {
            OrderError.CantAssignInStatus(status())
        }

        courierId = courier.id()
        status = OrderStatus.Assigned
    }

    fun complete() = either<OrderError, Unit> {
        ensure(status() == OrderStatus.Assigned) {
            OrderError.CantCompleteInStatus(status())
        }
        courierId = null
        status = OrderStatus.Completed
    }
}

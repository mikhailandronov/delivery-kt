package org.ama.delivery.core.domain.entities

import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.withError
import org.ama.delivery.core.domain.common.AbstractUuidId
import org.ama.delivery.core.domain.common.AggregateRoot
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.LocationError
import org.ama.delivery.core.domain.common.Name
import org.ama.delivery.core.domain.common.NameError
import org.ama.delivery.core.domain.common.Speed
import org.ama.delivery.core.domain.common.Volume
import org.ama.delivery.core.domain.common.VolumeError
import java.lang.Math.clamp
import java.util.UUID
import kotlin.math.abs


class CourierId(value: UUID = UUID.randomUUID()) : AbstractUuidId(value)

sealed class CourierError {
    data object CantAddStoragePlace : CourierError()
    data object NoEmptyStoragePlace : CourierError()
    data object MoveOperationFailed : CourierError()

    data class OrderVolumeExceedsAvailableStorage(val orderVolume: Volume) : CourierError()
    data class OrderNotFoundInStorage(val order: Order) : CourierError()
    data class StoragePlaceOperationFailed(val err: StoragePlaceError) : CourierError()
    data class OrderOperationFailed(val err: OrderError) : CourierError()
}

class Courier
private constructor(
    private val id: CourierId,
    val name: Name,
    val speed: Speed,
    private var location: Location,
    private val storagePlaces: MutableList<StoragePlace>
) : AggregateRoot<CourierId> {

    override fun id() = id

    fun location() = location
    fun storagePlaces() = storagePlaces.toList()

    companion object {

        fun create(name: Name, speed: Speed, location: Location) = either<CourierError, Courier> {
            val courier = reconstitute(CourierId(), name, speed, location)

            val defaultStorageVolume = withError({ err: VolumeError ->
                CourierError.CantAddStoragePlace
            }) {
                Volume.from(10).bind()
            }

            val defaultStorageName = withError({ err: NameError ->
                CourierError.CantAddStoragePlace
            }) {
                Name.from("Сумка").bind()
            }

            courier.addStoragePlace(defaultStorageName, defaultStorageVolume).bind()
            courier
        }

        fun reconstitute(
            id: CourierId, name: Name, speed: Speed, location: Location, storagePlaces: MutableList<StoragePlace> = mutableListOf()
        ) = Courier(id, name, speed, location, storagePlaces)
    }

    fun addStoragePlace(name: Name, volume: Volume) = either<CourierError, Unit> {
        val newStoragePlace = withError({ err: StoragePlaceError ->
            CourierError.CantAddStoragePlace
        }) {
            StoragePlace.create(name, volume).bind()
        }
        storagePlaces.add(newStoragePlace)
    }

    fun canTakeOrder(order: Order): Boolean =
        storagePlaces().find { it.isEmpty() && it.maxVolume >= order.volume } != null

    fun takeOrder(order: Order) = either<CourierError, Unit> {
        val availablePlaces = storagePlaces().filter { it.isEmpty() }
        ensure(availablePlaces.isNotEmpty()) {
            CourierError.NoEmptyStoragePlace
        }

        val suitablePlaces = availablePlaces.filter { it.maxVolume >= order.volume }
        ensure(suitablePlaces.isNotEmpty()) {
            CourierError.OrderVolumeExceedsAvailableStorage(order.volume)
        }

        withError({ err: OrderError ->
            CourierError.OrderOperationFailed(err)
        }) {
            order.assign(this@Courier).bind()
        }

        withError({ err: StoragePlaceError ->
            CourierError.StoragePlaceOperationFailed(err)
        }) {
            suitablePlaces.first().store(order.id(), order.volume).bind()
        }
    }

    fun completeOrder(order: Order) = either<CourierError, Unit> {
        val orderStoragePlace = storagePlaces().find { it.orderId() == order.id() }
        ensure(orderStoragePlace != null) {
            CourierError.OrderNotFoundInStorage(order)
        }

        withError({ err: StoragePlaceError ->
            CourierError.StoragePlaceOperationFailed(err)
        }) {
            orderStoragePlace.extract(order.id()).bind()
        }

        withError({ err: OrderError ->
            CourierError.OrderOperationFailed(err)
        }) {
            order.complete().bind()
        }
    }

    fun calculateTimeToLocation(targetLocation: Location): Double {
        val distance = location().distanceTo(targetLocation)
        val time = distance.toDouble() / speed.toInt()
        return time
    }

    fun move(destination: Location) = either {
        val difX = destination.xToInt() - location().xToInt()
        val difY = destination.yToInt() - location().yToInt()
        var cruisingRange = speed.toInt()

        val moveX = clamp(difX.toLong(), -cruisingRange, cruisingRange)
        cruisingRange -= abs(moveX)

        val moveY = clamp(difY.toLong(), -cruisingRange, cruisingRange)

        location = withError({ err: LocationError ->
            CourierError.MoveOperationFailed
        }) {
            Location.from(
                location.xToInt() + moveX,
                location.yToInt() + moveY
            ).bind()
        }
    }
}

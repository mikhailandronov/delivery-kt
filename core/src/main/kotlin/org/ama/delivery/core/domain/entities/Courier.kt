package org.ama.delivery.core.domain.entities

import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.withError
import org.ama.delivery.core.domain.common.AbstractUuidId
import org.ama.delivery.core.domain.common.AggregateRoot
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.Name
import org.ama.delivery.core.domain.common.NameError
import org.ama.delivery.core.domain.common.Speed
import java.util.UUID

class CourierId(value: UUID = UUID.randomUUID()) : AbstractUuidId(value)

sealed class CourierError {
    data object CantAddStoragePlace : CourierError()
    object NoEmptyStoragePlace : CourierError()
    data class OrderVolumeExceedsAvailableStorage(val orderVolume: Int) : CourierError()
    data class StoragePlaceOperationFailed(val err: StoragePlaceError) : CourierError()
    data class OrderNotFoundInStorage(val order: Order) : CourierError()
}

class Courier
private constructor(
    private val id: CourierId,
    val name: Name,
    val speed: Speed,
    private var location: Location,
    private val storagePlaces: MutableList<StoragePlace> = mutableListOf()
) : AggregateRoot<CourierId> {

    override fun id() = id

    fun location() = location
    fun storagePlaces() = storagePlaces.toList()

    companion object {

        fun create(name: Name, speed: Speed, location: Location) = either<CourierError, Courier> {
            val courier = reconstitute(CourierId(), name, speed, location)

            val defaultStorageVolume = 10
            val defaultStorageName = withError({ err: NameError ->
                CourierError.CantAddStoragePlace
            }) {
                Name.from("Сумка").bind()
            }
            courier.addStoragePlace(defaultStorageName, defaultStorageVolume).bind()
            courier
        }

        internal fun reconstitute(
            id: CourierId, name: Name, speed: Speed, location: Location
        ) = Courier(id, name, speed, location)

    }

    fun addStoragePlace(name: Name, volume: Int) = either<CourierError, Unit> {
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

        withError({ err: StoragePlaceError ->
            CourierError.StoragePlaceOperationFailed(err)
        }) {
            suitablePlaces.first().store(order.id(), order.volume)
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
            orderStoragePlace.extract(order.id())
        }
    }
}
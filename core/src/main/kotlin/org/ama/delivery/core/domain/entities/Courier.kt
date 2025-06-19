package org.ama.delivery.core.domain.entities

import arrow.core.raise.either
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
            val newId = CourierId()
            val courier = Courier(newId, name, speed, location)

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
        ) = either<CourierError, Courier> {
            Courier(id, name, speed, location)
        }
    }

    fun addStoragePlace(name: Name, volume: Int) = either<CourierError, Unit> {
        val newStoragePlace = withError({ err: StoragePlaceError ->
            CourierError.CantAddStoragePlace
        }) {
            StoragePlace.create(name, volume).bind()
        }
        storagePlaces.add(newStoragePlace)
    }

}
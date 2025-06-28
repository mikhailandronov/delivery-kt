package org.ama.delivery.core.domain.entities

import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ama.delivery.core.domain.common.Entity
import org.ama.delivery.core.domain.common.AbstractUuidId
import org.ama.delivery.core.domain.common.Name
import org.ama.delivery.core.domain.common.Volume
import java.util.UUID

class StoragePlaceId(value: UUID = UUID.randomUUID()) : AbstractUuidId(value)

sealed class StoragePlaceError {
    data class IncorrectVolume(val volume: Volume) : StoragePlaceError()
    data class ExcessiveVolume(val volume: Volume) : StoragePlaceError()
    data class OrderNotStored(val orderId: OrderId) : StoragePlaceError()

    data object StorageIsOccupied : StoragePlaceError()
    data object StorageIsEmpty : StoragePlaceError()
    data object StoringNotAllowed : StoragePlaceError()
}


class StoragePlace
private constructor(
    private val id: StoragePlaceId,
    val name: Name,
    val maxVolume: Volume,
    private var orderId: OrderId? = null,
    private var occupiedVolume: Volume = Volume.zeroVolume()
) : Entity<StoragePlaceId> {

    override fun id() = id
    fun orderId() = orderId
    fun occupiedVolume() = occupiedVolume

    fun isEmpty() = orderId() == null

    companion object {

        fun create(name: Name, maxVolume: Volume) = reconstitute(
            StoragePlaceId(), name, maxVolume
        )

        internal fun reconstitute(
            id: StoragePlaceId,
            name: Name,
            maxVolume: Volume
        ) = either<StoragePlaceError, StoragePlace> {
            ensure(maxVolume > Volume.zeroVolume()) {
                StoragePlaceError.IncorrectVolume(maxVolume)
            }

            StoragePlace(id, name, maxVolume)
        }
    }

    fun canStore(volume: Volume) = either<StoragePlaceError, Boolean> {
        ensure(volume > Volume.zeroVolume()) {
            StoragePlaceError.IncorrectVolume(volume)
        }

        isEmpty() && volume <= maxVolume
    }

    fun store(orderId: OrderId, volume: Volume) = either<StoragePlaceError, Unit> {
        val canStore = canStore(volume).bind()
        if (!canStore) {
            ensure(isEmpty()) {
                StoragePlaceError.StorageIsOccupied
            }

            ensure(volume <= maxVolume) {
                StoragePlaceError.ExcessiveVolume(volume)
            }

            raise(StoragePlaceError.StoringNotAllowed)
        }

        this@StoragePlace.orderId = orderId
        this@StoragePlace.occupiedVolume = volume
    }

    fun extract(orderId: OrderId) = either<StoragePlaceError, Unit> {
        ensure(!isEmpty()) {
            StoragePlaceError.StorageIsEmpty
        }

        ensure(orderId() == orderId){
            StoragePlaceError.OrderNotStored(orderId)
        }

        this@StoragePlace.orderId = null
        this@StoragePlace.occupiedVolume = Volume.zeroVolume()
    }

    override fun hashCode() = id.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoragePlace) return false
        return id() == other.id()
    }
}
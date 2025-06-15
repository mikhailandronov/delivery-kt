package org.ama.delivery.core.domain.entities

import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ama.delivery.core.domain.common.Entity
import org.ama.delivery.core.domain.common.AbstractUuidId
import java.util.UUID

class StoragePlaceId(value: UUID = UUID.randomUUID()) : AbstractUuidId(value)

sealed class StoragePlaceError {
    data class IncorrectName(val name: String) : StoragePlaceError()
    data class IncorrectVolume(val volume: Int) : StoragePlaceError()
    data class ExcessiveVolume(val volume: Int) : StoragePlaceError()
    data object StorageIsOccupied : StoragePlaceError()
    data object StorageIsEmpty : StoragePlaceError()
    data object StoringNotConfirmed : StoragePlaceError()
}


class StoragePlace
private constructor(
    private val id: StoragePlaceId,
    val name: String,
    val maxVolume: Int,
    private var orderId: OrderId? = null,
    private var occupiedVolume: Int = 0
) : Entity<StoragePlaceId> {

    override fun id() = id
    fun orderId() = orderId
    fun occupiedVolume() = occupiedVolume

    fun isEmpty() = orderId() == null

    companion object {

        fun create(name: String, maxVolume: Int) = either<StoragePlaceError, StoragePlace> {
            ensure(name.isNotBlank()) {
                StoragePlaceError.IncorrectName(name)
            }

            ensure(maxVolume > 0) {
                StoragePlaceError.IncorrectVolume(maxVolume)
            }

            val newId = StoragePlaceId()
            StoragePlace(newId, name, maxVolume)
        }

        internal fun reconstitute(
            id: StoragePlaceId, name: String, maxVolume: Int
        ) = either<StoragePlaceError, StoragePlace> {

            ensure(name.isNotBlank()) {
                StoragePlaceError.IncorrectName(name)
            }

            ensure(maxVolume > 0) {
                StoragePlaceError.IncorrectVolume(maxVolume)
            }

            StoragePlace(id, name, maxVolume)
        }
    }

    fun canStore(volume: Int) = either<StoragePlaceError, Boolean> {
        ensure(volume > 0) {
            StoragePlaceError.IncorrectVolume(volume)
        }

        isEmpty() && volume <= maxVolume
    }

    fun store(orderId: OrderId, volume: Int) = either<StoragePlaceError, Unit> {
        val canStore = canStore(volume).bind()
        if (!canStore) {
            ensure(isEmpty()) {
                StoragePlaceError.StorageIsOccupied
            }

            ensure(volume <= maxVolume) {
                StoragePlaceError.ExcessiveVolume(volume)
            }

            raise(StoragePlaceError.StoringNotConfirmed)
        }

        this@StoragePlace.orderId = orderId
        this@StoragePlace.occupiedVolume = volume
    }

    fun extract() = either<StoragePlaceError, Unit> {
        ensure(!isEmpty()) {
            StoragePlaceError.StorageIsEmpty
        }

        this@StoragePlace.orderId = null
        this@StoragePlace.occupiedVolume = 0
    }

    override fun hashCode() = id.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoragePlace) return false
        return id() == other.id()
    }
}
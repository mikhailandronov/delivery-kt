package org.ama.delivery.core.domain.entities

import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ama.delivery.core.domain.common.BaseUuidId
import org.ama.delivery.core.domain.common.Entity
import org.ama.delivery.core.domain.common.UuidIdFactory
import java.util.UUID


class StoragePlaceId
private constructor(id: UUID) : BaseUuidId(id) {
    companion object : UuidIdFactory<StoragePlaceId> {
        override fun from(id: UUID) = StoragePlaceId(id)
    }
}


sealed class StoragePlaceError {
    data class IncorrectName(val name: String) : StoragePlaceError()
    data class IncorrectVolume(val volume: Int) : StoragePlaceError()
    data class ExcessiveVolume(val volume: Int): StoragePlaceError()
}


class StoragePlace
private constructor(
    private val id: StoragePlaceId,
    val name: String,
    val maxVolume: Int,
    private var orderId: OrderId? = null
) : Entity<StoragePlaceId> {

    override fun id() = id

    fun name() = name
    fun maxVolume() = maxVolume
    fun orderId() = orderId

    fun isEmpty() = orderId() == null

    companion object {

        fun create(name: String, maxVolume: Int) = either<StoragePlaceError, StoragePlace>{
            ensure(name.isNotBlank()){
                StoragePlaceError.IncorrectName(name)
            }

            ensure(maxVolume > 0) {
                StoragePlaceError.IncorrectVolume(maxVolume)
            }

            val newId = StoragePlaceId.new()
            StoragePlace(newId, name, maxVolume)
        }
        internal fun reconstitute(id: StoragePlaceId, name: String, maxVolume: Int): StoragePlace {
            return StoragePlace(id, name, maxVolume)
        }
    }

    fun canStore(volume: Int) = either <StoragePlaceError, Boolean> {
        ensure (volume > 0){
            StoragePlaceError.IncorrectVolume(volume)
        }

        isEmpty() && volume <= maxVolume()
    }
}
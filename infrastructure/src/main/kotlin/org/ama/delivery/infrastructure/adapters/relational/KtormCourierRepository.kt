package org.ama.delivery.infrastructure.adapters.relational

import arrow.core.getOrElse
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.LocationError
import org.ama.delivery.core.domain.common.Name
import org.ama.delivery.core.domain.common.NameError
import org.ama.delivery.core.domain.common.Speed
import org.ama.delivery.core.domain.common.SpeedError
import org.ama.delivery.core.domain.common.Volume
import org.ama.delivery.core.domain.common.VolumeError
import org.ama.delivery.core.domain.entities.Courier
import org.ama.delivery.core.domain.entities.CourierId
import org.ama.delivery.core.domain.entities.StoragePlace
import org.ama.delivery.core.domain.entities.StoragePlaceId
import org.ama.delivery.core.ports.outbound.ICourierRepository
import org.ktorm.database.Database
import org.ktorm.dsl.Query
import org.ktorm.dsl.QueryRowSet
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.innerJoin
import org.ktorm.dsl.insert
import org.ktorm.dsl.isNotNull
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.map
import org.ktorm.dsl.notInList
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where

class KtormCourierRepository(private val database: Database) : ICourierRepository {
    override fun addNewCourier(courier: Courier) {
        if (KtormTransactionContext.get() == null)
            error("Operation should be run in transaction")

        database.insert(CouriersTable) {
            set(it.id, courier.id().toUUID())
            set(it.name, courier.name.toString())
            set(it.speed, courier.speed.toInt())
            set(it.locationX, courier.location().xToInt())
            set(it.locationY, courier.location().yToInt())
        }

        courier.storagePlaces().forEach { place ->
            database.insert(StoragePlacesTable) {
                set(it.id, place.id().toUUID())
                set(it.courierId, courier.id().toUUID())
                set(it.name, place.name.toString())
                set(it.maxVolume, place.maxVolume.toInt())
                set(it.orderId, place.orderId()?.toUUID())
            }
        }
    }

    override fun updateCourier(courier: Courier) {
        if (KtormTransactionContext.get() == null)
            error("Operation should be run in transaction")

        database.update(CouriersTable) {
            set(it.name, courier.name.toString())
            set(it.speed, courier.speed.toInt())
            set(it.locationX, courier.location().xToInt())
            set(it.locationY, courier.location().yToInt())
            where {
                it.id eq courier.id().toUUID()
            }
        }

        database.delete(StoragePlacesTable) {
            it.courierId eq courier.id().toUUID()
        }

        courier.storagePlaces().forEach { place ->
            database.insert(StoragePlacesTable) {
                set(it.id, place.id().toUUID())
                set(it.courierId, courier.id().toUUID())
                set(it.name, place.name.toString())
                set(it.maxVolume, place.maxVolume.toInt())
                set(it.orderId, place.orderId()?.toUUID())
            }
        }
    }

    override fun getCourierById(courierId: CourierId): Courier? {
        val query = database
            .from(CouriersTable)
            .leftJoin(StoragePlacesTable, on = StoragePlacesTable.courierId eq CouriersTable.id)
            .select(CouriersTable.columns + StoragePlacesTable.columns)
            .where { CouriersTable.id eq courierId.toUUID() }

        return constructCouriersFrom(query)
            .firstOrNull()
    }

    override fun getFreeCouriers(): List<Courier> {
        // Подзапрос для получения ID курьеров, у которых есть заказ
        val subquery = database
            .from(StoragePlacesTable)
            .select(StoragePlacesTable.courierId)
            .where { StoragePlacesTable.orderId.isNotNull() }

        // Основной запрос
        val query = database
            .from(StoragePlacesTable)
            .innerJoin(CouriersTable, on = StoragePlacesTable.courierId eq CouriersTable.id)
            .select(CouriersTable.columns + StoragePlacesTable.columns) // Выбираем все колонки из обеих таблиц
            .where { StoragePlacesTable.courierId notInList subquery }

        return constructCouriersFrom(query)
    }

    private fun constructCouriersFrom(query: Query): List<Courier> = query
        .map { row -> row.toCourierAndStoragePlacePair() }
        .groupBy { it.first.id() } // by courier.id
        .map {
            val list = it.value
            val courier = list.first().first
            val places = mutableListOf<StoragePlace>()
            list.forEach { pair ->
                val place = pair.second
                places.add(place)
            }
            Courier.reconstitute(
                courier.id(),
                courier.name,
                courier.speed,
                courier.location(),
                places
            )
        }

    private fun QueryRowSet.toCourierAndStoragePlacePair(): Pair<Courier, StoragePlace> {
        return Pair(this.toCourier(), this.toStoragePlace())
    }

    private fun QueryRowSet.toStoragePlace(): StoragePlace {
        val id = StoragePlaceId(this[StoragePlacesTable.id]!!)

        val name = Name.from(this[StoragePlacesTable.name]!!)
            .getOrElse { error ->
                when (error) {
                    is NameError.IncorrectNameValue -> throw IllegalArgumentException("Incorrect storage place name value: ${error.value}")
                }
            }

        val maxVolume = Volume.from(this[StoragePlacesTable.maxVolume]!!)
            .getOrElse { error ->
                when (error) {
                    is VolumeError.IncorrectVolumeValue -> throw IllegalArgumentException("Incorrect storage place volume value: ${error.value}")
                }
            }

        val place = StoragePlace.reconstitute(id, name, maxVolume)
            .getOrElse {
                throw IllegalArgumentException("Cannot create storage place")
            }

        return place
    }

    private fun QueryRowSet.toCourier(): Courier {
        val id = CourierId(this[CouriersTable.id]!!)

        val name = Name.from(this[CouriersTable.name]!!)
            .getOrElse { error ->
                when (error) {
                    is NameError.IncorrectNameValue -> throw IllegalArgumentException("Incorrect courier name value: ${error.value}")
                }
            }

        val speed = Speed.from(this[CouriersTable.speed]!!)
            .getOrElse { error ->
                when (error) {
                    is SpeedError.IncorrectSpeedValue -> throw IllegalArgumentException("Incorrect courier speed value: ${error.value}")
                }
            }

        val locX = this[CouriersTable.locationX]!!
        val locY = this[CouriersTable.locationY]!!
        val location = Location.from(locX, locY)
            .getOrElse { error ->
                when (error) {
                    is LocationError.IncorrectCoordinates -> throw IllegalArgumentException("Incorrect courier coordinates: (${error.x}, ${error.y})")
                }
            }

        return Courier.reconstitute(id, name, speed, location)
    }

}



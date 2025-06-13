package org.ama.delivery.core.domain.common

import java.util.UUID

abstract class BaseUuidId(
    protected val id: UUID
) : ValueObject {

    fun toUUID(): UUID = id

    override fun toString(): String = id.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseUuidId
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

interface UuidIdFactory<T : BaseUuidId> {
    fun from(id: UUID): T
    fun from(id: String): T = from(UUID.fromString(id))
    fun new(): T = from(UUID.randomUUID())
}
package org.ama.delivery.core.domain.common

import java.util.UUID

interface UuidId : ValueObject {
    fun toUUID(): UUID
}

abstract class AbstractUuidId(
    private val value: UUID
): UuidId{
    override fun toUUID(): UUID = value

    override fun toString(): String = "${this.javaClass.simpleName}(value=$value)"

    override fun hashCode() = value.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractUuidId
        return value == other.value
    }
}
package org.ama.delivery.core.domain.common

import arrow.core.raise.either
import arrow.core.raise.ensure

sealed class VolumeError {
    data class IncorrectVolumeValue(val value: Int) : VolumeError()
}

@ConsistentCopyVisibility
data class Volume internal constructor(
    private val value: Int,
) : ValueObject, Comparable<Volume> {
    fun toInt() = value
    override fun compareTo(other: Volume) = this.value.compareTo(other.value)

    companion object {
        fun zeroVolume() = Volume(0)

        fun from(value: Int) = either<VolumeError, Volume> {
            ensure(
                value >= zeroVolume().toInt()
            ) { VolumeError.IncorrectVolumeValue(value) }
            Volume(value)
        }
    }
}
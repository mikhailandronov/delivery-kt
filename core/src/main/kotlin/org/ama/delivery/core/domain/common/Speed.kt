package org.ama.delivery.core.domain.common

import arrow.core.raise.either
import arrow.core.raise.ensure

sealed class SpeedError {
    data class IncorrectSpeedValue(val value: Int) : SpeedError()
}

@ConsistentCopyVisibility
data class Speed internal constructor(
    private val value: Int,
) : ValueObject {
    fun toInt() = value

    companion object {
        fun minSpeed() = Speed(1)

        fun from(value: Int) = either<SpeedError, Speed> {
            ensure(
                value >= minSpeed().toInt()
            ) { SpeedError.IncorrectSpeedValue(value) }
            Speed(value)
        }
    }
}
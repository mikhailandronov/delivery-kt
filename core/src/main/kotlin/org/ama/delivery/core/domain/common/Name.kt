package org.ama.delivery.core.domain.common

import arrow.core.raise.either
import arrow.core.raise.ensure

sealed class NameError {
    data class IncorrectNameValue(val value: String) : NameError()
}

@ConsistentCopyVisibility
data class Name private constructor(
    private val value: String,
) : ValueObject {
    override fun toString() = value

    companion object {
        fun from(value: String) = either<NameError, Name> {
            ensure(
                value.isNotBlank()
            ) { NameError.IncorrectNameValue(value) }

            Name(value)
        }
    }
}
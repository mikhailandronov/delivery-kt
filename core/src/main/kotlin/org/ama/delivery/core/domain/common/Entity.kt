package org.ama.delivery.core.domain.common

interface Entity<out IdType: ValueObject> {
    fun id():IdType
}
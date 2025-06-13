package org.ama.delivery.core.domain.common

interface Entity<IdType: ValueObject> {
    fun id():IdType
}
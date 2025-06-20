package org.ama.delivery.core.domain.entities

import org.ama.delivery.core.domain.common.ValueObject

enum class OrderStatus(val id: Int): ValueObject {
    Created(1),
    Assigned(2),
    Completed(3)
}
package org.ama.delivery.core.domain.entities

import org.ama.delivery.core.domain.common.AbstractUuidId
import org.ama.delivery.core.domain.common.AggregateRoot
import java.util.UUID

class OrderId(value: UUID = UUID.randomUUID()) : AbstractUuidId(value)

class Order: AggregateRoot<OrderId> {
    override fun id(): OrderId {
        TODO("Not yet implemented")
    }
}
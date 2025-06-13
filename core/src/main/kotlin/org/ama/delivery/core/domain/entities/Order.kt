package org.ama.delivery.core.domain.entities

import org.ama.delivery.core.domain.common.AggregateRoot
import org.ama.delivery.core.domain.common.BaseUuidId
import org.ama.delivery.core.domain.common.UuidIdFactory
import java.util.UUID


class OrderId
private constructor(id: UUID) : BaseUuidId(id) {
    companion object : UuidIdFactory<OrderId> {
        override fun from(id: UUID) = OrderId(id)
    }
}

class Order: AggregateRoot<OrderId> {
    override fun id(): OrderId {
        TODO("Not yet implemented")
    }
}
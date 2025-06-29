package org.ama.delivery.core.domain.services

import arrow.core.Either
import org.ama.delivery.core.domain.entities.Courier
import org.ama.delivery.core.domain.entities.Order

sealed class DispatchError {
    data object NoSuitableCourier : DispatchError()
    data object EmptyCourierList: DispatchError()
    data class CourierRejectedTheOrder(val courier: Courier, val order: Order) : DispatchError()
}

interface IDispatchService {
    fun dispatch(order: Order, couriers: List<Courier>): Either<DispatchError, Courier>
}
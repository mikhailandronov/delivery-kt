package org.ama.delivery.core.domain.services

import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.withError
import org.ama.delivery.core.domain.entities.Courier
import org.ama.delivery.core.domain.entities.CourierError
import org.ama.delivery.core.domain.entities.Order

class DispatchService : IDispatchService {
    override fun dispatch(
        order: Order,
        couriers: List<Courier>
    ) = either<DispatchError, Courier> {
        ensure(couriers.isNotEmpty()) {
            DispatchError.EmptyCourierList
        }

        val couriersWithTime: List<Pair<Courier, Double>> = couriers
            .filter { it.canTakeOrder(order) }
            .map { courier ->
                val time = courier.calculateTimeToLocation(order.destination)
                courier to time
            }

        ensure(couriersWithTime.isNotEmpty()) {
            DispatchError.NoSuitableCourier
        }

        val selectedCourier = couriersWithTime   // courier with min time
            .minBy { it.second }
            .first

        withError({ err: CourierError ->
            DispatchError.CourierRejectedTheOrder(selectedCourier, order)
        }) {
            selectedCourier.takeOrder(order).bind()
        }

        selectedCourier
    }
}
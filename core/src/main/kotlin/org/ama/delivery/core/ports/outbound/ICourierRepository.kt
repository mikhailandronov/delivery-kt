package org.ama.delivery.core.ports.outbound

import org.ama.delivery.core.domain.entities.Courier
import org.ama.delivery.core.domain.entities.CourierId

interface ICourierRepository {
    fun addNewCourier(courier: Courier)
    fun updateCourier(courier: Courier)
    fun getCourierById(courierId: CourierId): Courier?
    fun getFreeCouriers(): List<Courier>
}
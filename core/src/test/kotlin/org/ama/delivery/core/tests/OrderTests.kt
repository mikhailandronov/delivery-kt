package org.ama.delivery.core.tests

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.entities.Order
import org.ama.delivery.core.domain.entities.OrderId
import org.ama.delivery.core.domain.entities.OrderStatus

class OrderTests: BehaviorSpec({
    context("correct creation") {
        given("order id, location, volume") {
            val id = OrderId()
            val destination = Location.minLocation()
            val volume = 5

            When("courier is created") {
                val created = Order.create(id, destination, volume).shouldBeRight()
                then("it has correct attributes") {
                    created.id() shouldBeEqual id
                    created.destination shouldBeEqual destination
                    created.volume shouldBeEqual volume
                }
                then("it has correct default state") {
                    created.status() shouldBe OrderStatus.Created
                    created.courierId() shouldBe null
                }
            }
        }
    }
    context("order assignment") {

    }
    context("order completion") {

    }
})
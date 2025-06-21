package org.ama.delivery.core.tests

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.Name
import org.ama.delivery.core.domain.common.Speed
import org.ama.delivery.core.domain.common.Volume
import org.ama.delivery.core.domain.entities.Courier
import org.ama.delivery.core.domain.entities.Order
import org.ama.delivery.core.domain.entities.OrderError
import org.ama.delivery.core.domain.entities.OrderId
import org.ama.delivery.core.domain.entities.OrderStatus

class OrderTests: BehaviorSpec({
    context("correct creation") {
        given("order id, location, volume") {
            val id = OrderId()
            val destination = Location.minLocation()
            val volume5 = Volume.from(5).shouldBeRight()

            When("courier is created") {
                val created = Order.create(id, destination, volume5).shouldBeRight()
                then("it has correct attributes") {
                    created.id() shouldBeEqual id
                    created.destination shouldBeEqual destination
                    created.volume shouldBeEqual volume5
                }
                then("it has correct default state") {
                    created.status() shouldBe OrderStatus.Created
                    created.courierId() shouldBe null
                }
            }
        }
    }
    context("order assignment and completion") {
        given("newly created order and courier"){
            val id = OrderId()
            val destination = Location.minLocation()
            val volume5 = Volume.from(5).shouldBeRight()
            val order = Order.create(id, destination, volume5).shouldBeRight()
            order.status() shouldBe OrderStatus.Created

            val name = Name.from("Test courier").shouldBeRight()
            val speed = Speed.minSpeed()
            val location = Location.minLocation()
            val courier = Courier.create(name, speed, location).shouldBeRight()

            When("order completion requested before assignment"){
                then("it returns an error, no changes"){
                    order.complete().shouldBeLeft(
                        OrderError.CantCompleteInStatus(OrderStatus.Created)
                    )
                    order.status() shouldBe OrderStatus.Created
                }
            }
            When("order is assigned for first time"){
                then("it is assigned successfully"){
                    order.assign(courier).shouldBeRight()
                    order.courierId() shouldBe courier.id()
                    order.status() shouldBe OrderStatus.Assigned
                }
            }
            When("order is assigned again"){
                then("it returns an error, no changes"){
                    order.assign(courier).shouldBeLeft(
                        OrderError.CantAssignInStatus(OrderStatus.Assigned)
                    )
                    order.courierId() shouldBe courier.id()
                    order.status() shouldBe OrderStatus.Assigned
                }
            }
            When("order completion requested after assignment"){
                then("it is completed successfully"){
                    order.complete().shouldBeRight ()
                    order.courierId() shouldBe null
                    order.status() shouldBe OrderStatus.Completed
                }
            }
        }
    }
})
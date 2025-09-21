package org.ama.delivery.core.tests

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.koin.KoinExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.Name
import org.ama.delivery.core.domain.common.Speed
import org.ama.delivery.core.domain.common.Volume
import org.ama.delivery.core.domain.entities.Courier
import org.ama.delivery.core.domain.entities.Order
import org.ama.delivery.core.domain.entities.OrderId
import org.ama.delivery.core.domain.entities.OrderStatus
import org.ama.delivery.core.domain.services.DispatchError
import org.ama.delivery.core.domain.services.IDispatchService

class DispatchServiceTests : BehaviorSpec(), KoinTest {
    init {
        extension(KoinExtension(testModuleCore))

        val dispatcher: IDispatchService by inject()

        context("dispatch order to courier ") {
            given("an order and a list of three couriers") {
                val orderDestination = Location.maxLocation()
                val volume5 = Volume.from(5).shouldBeRight()
                val smallOrder = Order.create(OrderId(), orderDestination, volume5).shouldBeRight()
                val volume15 = Volume.from(15).shouldBeRight()
                val exceedingVolumeOrder = Order.create(OrderId(), orderDestination, volume15).shouldBeRight()

                val courierLocation = Location.minLocation()

                val slowName = Name.from("Slow courier").shouldBeRight()
                val slowSpeed = Speed.minSpeed()
                val slowCourier = Courier.create(slowName, slowSpeed, courierLocation).shouldBeRight()

                val mediumName = Name.from("Medium courier").shouldBeRight()
                val mediumSpeed = Speed.from(2).shouldBeRight()
                val mediumCourier = Courier.create(mediumName, mediumSpeed, courierLocation).shouldBeRight()

                val fastName = Name.from("Fast courier").shouldBeRight()
                val fastSpeed = Speed.from(3).shouldBeRight()
                val fastCourier = Courier.create(fastName, fastSpeed, courierLocation).shouldBeRight()

                val couriers = listOf(slowCourier, mediumCourier, fastCourier)

                When("try to dispatch an order to an empty list") {
                    then("an error should be returned, order doesn't change") {
                        dispatcher.dispatch(smallOrder, emptyList())
                            .shouldBeLeft(
                                DispatchError.EmptyCourierList
                            )
                        smallOrder.status() shouldBe OrderStatus.Created
                    }
                }
                When("try to dispatch a freshly created order") {
                    then("it is dispatched to the fastest courier") {
                        val selectedCourier = dispatcher.dispatch(smallOrder, couriers).shouldBeRight()
                        selectedCourier shouldBe fastCourier
                        smallOrder.status() shouldBe OrderStatus.Assigned
                    }
                }
                When("try to dispatch an assigned order again") {
                    then("an error should be returned, order doesn't change") {
                        smallOrder.status() shouldBe OrderStatus.Assigned
                        val selectedCourier = dispatcher.dispatch(smallOrder, couriers)
                        selectedCourier.shouldBeLeft(
                            DispatchError.CourierRejectedTheOrder(mediumCourier, smallOrder)
                        )
                        smallOrder.status() shouldBe OrderStatus.Assigned
                    }
                }
                When("try to dispatch an exceeding volume order") {
                    then("an error should be returned, order doesn't change") {
                        dispatcher.dispatch(exceedingVolumeOrder, couriers)
                            .shouldBeLeft(
                                DispatchError.NoSuitableCourier
                            )

                        exceedingVolumeOrder.status() shouldBe OrderStatus.Created
                    }
                }
                When("extend storage place and try to dispatch big volume order") {
                    val extraPlaceName = Name.from("Кузов").shouldBeRight()
                    slowCourier.addStoragePlace(extraPlaceName, volume15).shouldBeRight()

                    then("it is dispatched to the fastest suitable courier") {
                        val selectedCourier = dispatcher
                            .dispatch(exceedingVolumeOrder, couriers)
                            .shouldBeRight()

                        selectedCourier shouldBe slowCourier
                        exceedingVolumeOrder.status() shouldBe OrderStatus.Assigned
                    }
                }
            }
        }
    }
}
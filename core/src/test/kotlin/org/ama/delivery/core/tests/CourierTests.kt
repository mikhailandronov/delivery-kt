package org.ama.delivery.core.tests

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.Name
import org.ama.delivery.core.domain.common.Speed
import org.ama.delivery.core.domain.entities.Courier
import org.ama.delivery.core.domain.entities.CourierError
import org.ama.delivery.core.domain.entities.CourierId
import org.ama.delivery.core.domain.entities.Order

class CourierTests: BehaviorSpec({
    context("correct creation / reconstitution") {
        given("name, speed, location"){
            val name = Name.from("Test courier").shouldBeRight()
            val speed = Speed.minSpeed()
            val location = Location.minLocation()

            When("courier is created"){
                val created = Courier.create(name, speed, location).shouldBeRight()
                then("it has correct attributes"){
                    created.name shouldBeEqual name
                    created.speed shouldBeEqual speed
                    created.location() shouldBeEqual location
                }
                then("it has correct storage places"){
                    val places = created.storagePlaces()
                    val defaultVolume = 10
                    places.size shouldBe 1
                    places[0].maxVolume shouldBe defaultVolume
                    places[0].name.toString() shouldBe "Сумка"
                }
            }

            When("courier is restored with id"){
                val id = CourierId()
                val restored = Courier.reconstitute(id, name, speed, location)
                then("it has correct attributes"){
                    restored.id() shouldBeEqual id
                    restored.name shouldBeEqual name
                    restored.speed shouldBeEqual speed
                    restored.location() shouldBeEqual location
                }
                then("it has no storage places"){
                    restored.storagePlaces().isEmpty() shouldBe true
                }
            }
        }
    }
    context("adding storage place") {
        given("a newly created courier"){
            val name = Name.from("Test courier").shouldBeRight()
            val speed = Speed.minSpeed()
            val location = Location.minLocation()
            val courier = Courier.create(name, speed, location).shouldBeRight()

            val correctPlaceName = Name.from("New place").shouldBeRight()
            val correctVolume = 20
            val incorrectVolume = -1

            When("incorrect values are used to add a place"){
                then("an error should be returned on adding"){
                    courier.addStoragePlace(correctPlaceName, incorrectVolume)
                        .shouldBeLeft(CourierError.CantAddStoragePlace)
                }
            }
            When("correct values are used to add a place"){
                then("a storage place is added successfully"){
                    courier.addStoragePlace(correctPlaceName, correctVolume).shouldBeRight()
                    val places = courier.storagePlaces()
                    places.size shouldBe 2
                    places[1].name shouldBe correctPlaceName
                    places[1].maxVolume shouldBe correctVolume
                }
            }
        }

    }

    context("checking capacity") {
        given("a courier with default storage place and two orders"){
            val name = Name.from("Test courier").shouldBeRight()
            val speed = Speed.minSpeed()
            val location = Location.minLocation()
            val courier = Courier.create(name, speed, location).shouldBeRight()

            val orderFitsStorage = Order.create(Location.maxLocation(), 10).shouldBeRight()
            val orderExceedsStorage = Order.create(Location.maxLocation(), 20).shouldBeRight()

            When("free capacity is available for the order"){
                then("courier can take order"){
                    courier.canTakeOrder(orderFitsStorage) shouldBe true
                }
            }
            When("order volume is larger than available capacity"){
                then("courier can not take order"){
                    courier.canTakeOrder(orderExceedsStorage) shouldBe false
                }
            }

        }
        given("a courier without storage place"){
            val name = Name.from("Test courier").shouldBeRight()
            val speed = Speed.minSpeed()
            val location = Location.minLocation()
            val courier = Courier.reconstitute(CourierId(), name, speed, location)

            val order = Order.create(Location.maxLocation(), 1).shouldBeRight()

            When("check with any order volume"){
                then("courier can not take order"){
                    courier.canTakeOrder(order) shouldBe false
                }
            }
        }
    }

    context("taking order") {
        given("a courier with two storage places and two orders"){
            val courierName = Name.from("Test courier").shouldBeRight()
            val speed = Speed.minSpeed()
            val location = Location.minLocation()
            val courier = Courier.create(courierName, speed, location).shouldBeRight()

            val smallPlaceName = Name.from("Small place").shouldBeRight()
            courier.addStoragePlace(smallPlaceName, 10).shouldBeRight()

            val orderFitsStorage = Order.create(Location.maxLocation(), 10).shouldBeRight()
            val orderExceedsStorage = Order.create(Location.maxLocation(), 20).shouldBeRight()

            When("storage place for first order is available"){
                then("courier takes the order"){
                    courier.takeOrder(orderFitsStorage).shouldBeRight()
                    courier.storagePlaces()
                        .filter { it.orderId() == orderFitsStorage.id() }
                        .size shouldBe 1
                    courier.storagePlaces()
                        .filter { it.orderId() == null }
                        .size shouldBe 1
                }
            }
            When("storage place for second order is not suitable"){
                then("courier can't take the order"){
                    courier.takeOrder(orderExceedsStorage).shouldBeLeft(
                        CourierError.OrderVolumeExceedsAvailableStorage(orderExceedsStorage.volume)
                    )
                    courier.storagePlaces()
                        .filter { it.orderId() == orderExceedsStorage.id() }
                        .size shouldBe 0
                    courier.storagePlaces()
                        .filter { it.orderId() == null }
                        .size shouldBe 1
                }
            }
        }
    }

    context("completing order") {

    }

    context("calculating steps") {

    }

    context("moving a step towards location") {

    }
})

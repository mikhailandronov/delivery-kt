package org.ama.delivery.core.tests

import arrow.core.getOrElse
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.LocationError

class LocationTests : BehaviorSpec({
    context("correct creation") {
        given("an arbitrary coordinates") {
            val correctX = 5
            val correctY = 9

            val valuesX = arrayOf(
                Location.maxLocation().xToInt() + 1,
                Location.minLocation().xToInt() - 1,
                Location.minLocation().xToInt() // to combine correct and incorrect coordinates
            )

            val valuesY = arrayOf(
                Location.maxLocation().yToInt() + 1,
                Location.minLocation().yToInt() - 1,
                Location.maxLocation().yToInt() // to combine correct and incorrect coordinates
            )

            When("trying to create location outside the bounds") {
                for (incorrectX in valuesX)
                    for (incorrectY in valuesY)
                        if (incorrectX != Location.minLocation().xToInt() ||
                            incorrectY != Location.maxLocation().yToInt()
                        ) {
                            val createdLocation = Location.from(incorrectX, incorrectY)

                            then("($incorrectX, $incorrectY): an error should be returned") {
                                createdLocation.shouldBeLeft(LocationError.IncorrectCoordinates(incorrectX, incorrectY))
                            }
                        }
            }
            When("trying to create location inside the bounds") {
                val result = Location.from(correctX, correctY)

                then("correct location should be created") {
                    val createdLocation = result.shouldBeRight()
                    createdLocation.xToInt() shouldBe correctX
                    createdLocation.yToInt() shouldBe correctY
                }
            }
        }
    }
    context("equality check") {
        given("three location objects: loc1 == loc2 != loc3"){
            val loc1 = Location.from(5, 5).getOrElse { Location.minLocation() }
            val loc2 = Location.from(5, 5).getOrElse { Location.minLocation() }
            val loc3 = Location.from(2, 3).getOrElse { Location.minLocation() }

            When("coordinates are equal"){
                then("loc1 should be equal to loc2"){
                    loc1 shouldBeEqual loc2
                }
            }
            When("coordinates are not equal"){
                then("loc2 should not be equal to loc3"){
                    loc2 shouldNotBeEqual loc3
                }
            }
        }
    }
    context("distance calculation") {
        given("three location objects: loc1 == loc2 != loc3"){
            val loc1 = Location.from(5, 5).getOrElse { Location.minLocation() }
            val loc2 = Location.from(5, 5).getOrElse { Location.minLocation() }
            val loc3 = Location.from(2, 3).getOrElse { Location.minLocation() }

            When("locations are equal"){
                val distZero = loc1.distanceTo(loc2)
                then("distance should be 0"){
                    distZero shouldBe 0
                }
            }
            When("locations are not equal"){
                val distNonZero = loc2.distanceTo(loc3)
                then("distance should be 5"){
                    distNonZero shouldBe 5
                }
            }
        }
    }
    context("random location generation") {
        When("random object requested"){
            val randomLocation = Location.random()
            then("it should be successfully created"){
                randomLocation.shouldBeInstanceOf<Location>()
            }
        }
    }
})
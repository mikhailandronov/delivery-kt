package org.ama.delivery.core.tests

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import org.ama.delivery.core.domain.entities.OrderId
import org.ama.delivery.core.domain.entities.StoragePlace
import org.ama.delivery.core.domain.entities.StoragePlaceError

class StoragePlaceTests : BehaviorSpec({
    context("correct creation") {
        given("name and volume values") {
            val correctName = "Correct storage place"
            val incorrectName = ""
            val correctVolumeValue = 10
            val incorrectVolumeValues = arrayOf(-1, 0)

            When("name is incorrect") {
                then("an error should be returned") {
                    StoragePlace.create(incorrectName, correctVolumeValue)
                        .shouldBeLeft(StoragePlaceError.IncorrectName(incorrectName))
                }
            }
            When("volume is incorrect") {
                for (incorrectVolume in incorrectVolumeValues) {
                    then("${incorrectVolume}: an error should be returned") {
                        StoragePlace.create(correctName, incorrectVolume)
                            .shouldBeLeft(StoragePlaceError.IncorrectVolume(incorrectVolume))
                    }
                }
            }
            When("name and volume are correct") {
                val result = StoragePlace.create(correctName, correctVolumeValue)
                then("correct storage place should be created") {
                    val createdPlace = result.shouldBeRight()
                    createdPlace.name shouldBe correctName
                    createdPlace.maxVolume shouldBe correctVolumeValue
                }
            }
        }
    }
    context("checking ability to store") {
        given("an empty storage place") {
            val place = StoragePlace.create("Test place", 10).shouldBeRight()

            When("try to store incorrect volume") {
                val incorrectVolume = -2
                then("an error should be returned") {
                    place.canStore(incorrectVolume)
                        .shouldBeLeft(StoragePlaceError.IncorrectVolume(incorrectVolume))
                }
            }
            When("try to store excessive volume") {
                val excessiveVolume = 11
                then("should return false") {
                    val result = place.canStore(excessiveVolume).shouldBeRight()
                    result shouldBe false
                }
            }
            When("try to store correct volume") {
                val correctVolume = 9
                then("should return true") {
                    val result = place.canStore(correctVolume).shouldBeRight()
                    result shouldBe true
                }
            }
        }
    }
    context("storing items") {

    }

    context("extracting items") {

    }
})
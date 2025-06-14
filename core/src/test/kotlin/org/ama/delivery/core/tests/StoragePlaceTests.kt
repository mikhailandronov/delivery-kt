package org.ama.delivery.core.tests

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
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

            When("check storage with incorrect volume") {
                val incorrectVolume = -2
                then("an error should be returned") {
                    place.canStore(incorrectVolume)
                        .shouldBeLeft(StoragePlaceError.IncorrectVolume(incorrectVolume))
                }
            }
            When("check storage with excessive volume") {
                val excessiveVolume = 11
                then("should return false") {
                    val result = place.canStore(excessiveVolume).shouldBeRight()
                    result shouldBe false
                }
            }
            When("check storage with correct volume") {
                val correctVolume = 9
                then("should return true") {
                    val result = place.canStore(correctVolume).shouldBeRight()
                    result shouldBe true
                }
            }
        }
    }

    context("storing items") {
        given("an empty storage place and order id") {
            val place = StoragePlace.create("Test place", 10).shouldBeRight()
            val orderId = OrderId()

            When("try to store incorrect volume") {
                val incorrectVolume = -2
                then("an error should be returned") {
                    place.store(orderId, incorrectVolume)
                        .shouldBeLeft(StoragePlaceError.IncorrectVolume(incorrectVolume))
                }
            }
            When("try to store excessive volume") {
                val excessiveVolume = 11
                then("an error should be returned") {
                    place.store(orderId, excessiveVolume)
                        .shouldBeLeft(StoragePlaceError.ExcessiveVolume(excessiveVolume))
                }
            }
            When("try to store correct volume") {
                val correctVolume = 9
                then("should store it successfully") {
                    place.store(orderId, correctVolume).shouldBeRight()
                    place.occupiedVolume() shouldBe correctVolume
                }
            }
            When("try to store to occupied storage place") {
                val storedVolume = place.occupiedVolume()
                val additionalVolume = 9
                then("an error should be returned, no changes") {
                    place.store(orderId, additionalVolume)
                        .shouldBeLeft(StoragePlaceError.StorageIsOccupied)
                    place.occupiedVolume() shouldBe storedVolume
                }
            }
        }
    }

    context("extracting items") {
        given("an occupied storage place") {
            val place = StoragePlace.create("Test place", 10).shouldBeRight()
            val orderId = OrderId()
            place.store(orderId, 10).shouldBeRight()

            When("try to extract item from occupied storage") {
                if (!place.isEmpty())
                    then("should extract it successfully") {
                        place.extract().shouldBeRight()
                        place.isEmpty() shouldBe true
                        place.occupiedVolume() shouldBe 0
                    }
            }
            When("try to extract item from empty storage") {
                if (place.isEmpty())
                    then("an error should be returned, no changes") {
                        place.extract().shouldBeLeft(StoragePlaceError.StorageIsEmpty)
                        place.isEmpty() shouldBe true
                        place.occupiedVolume() shouldBe 0
                    }
            }
        }
    }

    context("equality check") {
        given("three entities: place1 == place2 != place3") {
            val place1 = StoragePlace.create("Place1", 10).shouldBeRight()
            val place2 = StoragePlace.reconstitute(
                place1.id(), place1.name, place1.maxVolume
            ).shouldBeRight()
            val place3 = StoragePlace.create("Place3", 10)

            When("ids are equal") {
                then("place1 should be equal to place2") {
                    place1 shouldBeEqual place2
                }
                then("place1 and place2 hash-codes should be equal") {
                    place1.hashCode() shouldBeEqual place2.hashCode()
                }
            }
            When("ids are not equal") {
                then("place2 should not be equal to place3") {
                    place2 shouldNotBeEqual place3
                }
                then("place2 and place3 hash-codes should not be equal") {
                    place2.hashCode() shouldNotBeEqual place3.hashCode()
                }
            }
        }
    }
})
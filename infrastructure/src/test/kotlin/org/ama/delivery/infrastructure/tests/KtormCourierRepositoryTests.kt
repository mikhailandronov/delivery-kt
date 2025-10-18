package org.ama.delivery.infrastructure.tests

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.Name
import org.ama.delivery.core.domain.common.Speed
import org.ama.delivery.core.domain.entities.Courier
import org.ama.delivery.core.domain.entities.CourierId
import org.ama.delivery.core.ports.outbound.ICourierRepository
import org.ama.delivery.core.ports.outbound.ITransactionManager
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.UUID
import kotlin.getValue

@Suppress("unused")
class KtormCourierRepositoryTests : BehaviorSpec(), KoinTest {
    init {
        extension(KoinExtension(testModuleInfra))

        context("get courier by id") {
            given("a repository object") {
                val repo: ICourierRepository by inject()

                When("request with existing id") {
                    val existingId = CourierId(UUID.fromString("e2f7e7aa-cb1e-455c-9e85-b3e776ba9d12"))

                    then("the correct Courier object should be returned") {
                        val foundCourier = repo.getCourierById(existingId)
                        foundCourier shouldNotBe null
                        foundCourier?.id() shouldBe existingId
                        foundCourier?.storagePlaces()?.count() shouldBe 2
                    }
                }

                When("request with non-existing id") {
                    then("null value should be returned") {
                        val foundCourier = repo.getCourierById(CourierId())
                        foundCourier shouldBe null
                    }
                }
            }
        }

        context("get free couriers") {
            given("a repository object") {
                val repo: ICourierRepository by inject()

                When("trying to get free couriers") {
                    then("correct list of Courier objects should be returned") {
                        val freeCouriers = repo.getFreeCouriers()
                        freeCouriers shouldNotBe null
                        freeCouriers.forEach {
                            println("id: ${it.id()}, name: ${it.name}, speed: ${it.speed}, places: ${it.storagePlaces().count()}")
                        }
                    }
                }
            }
        }

        context("add new courier") {
            given("a repository object and transaction manager") {
                val repo: ICourierRepository by inject()
                val txMgr: ITransactionManager by inject()

                When("try to save newly created Courier") {
                    val name = Name.from("Test courier").shouldBeRight()
                    val speed = Speed.minSpeed()
                    val location = Location.minLocation()
                    val courier = Courier.create(name, speed, location).shouldBeRight()

                    then("the correct Courier object should be saved") {
                        txMgr.transactional {
                            repo.addNewCourier(courier)
                        }
                    }
                }
            }
        }

        context("update courier") {
            given("a repository object and transaction manager") {
                val repo: ICourierRepository by inject()
                val txMgr: ITransactionManager by inject()

                When("try to update existing Courier") {
                    then("the Courier should be updated successfully") {
                        val existingId = CourierId(UUID.fromString("e2f7e7aa-cb1e-455c-9e85-b3e776ba9d12"))
                        val existingCourier = repo.getCourierById(existingId)

                        existingCourier shouldNotBe null
                        
                        txMgr.transactional {
                            repo.updateCourier(existingCourier!!)
                        }
                        
                        // Verify the courier was updated
                        val updatedCourier = repo.getCourierById(existingId)
                        updatedCourier shouldNotBe null
                        updatedCourier?.id() shouldBe existingId
                        updatedCourier?.name shouldBe existingCourier?.name
                        updatedCourier?.speed shouldBe existingCourier?.speed
                        updatedCourier?.location() shouldBe existingCourier?.location()
                        updatedCourier?.storagePlaces()?.count() shouldBe existingCourier?.storagePlaces()?.count()
                    }
                }

                When("try to update Courier with modified data") {
                    then("the Courier should be updated correctly (and then updated back)") {
                        val existingId = CourierId(UUID.fromString("e2f7e7aa-cb1e-455c-9e85-b3e776ba9d12"))
                        val existingCourier = repo.getCourierById(existingId)

                        existingCourier shouldNotBe null
                        existingCourier?.name.toString() shouldBe "Анна Сидорова"
                        
                        // Create updated courier with same ID but new name
                        val newName = Name.from("Updated Name").shouldBeRight()
                        val updatedCourier = Courier.reconstitute(
                            existingCourier!!.id(),
                            newName,
                            existingCourier.speed,
                            existingCourier.location(),
                            existingCourier.storagePlaces().toMutableList()
                        )

                        txMgr.transactional {
                            repo.updateCourier(updatedCourier)
                        }
                        
                        // Verify the courier was updated
                        val retrievedCourier = repo.getCourierById(existingId)
                        retrievedCourier shouldNotBe null
                        retrievedCourier?.id() shouldBe existingId
                        retrievedCourier?.name shouldBe newName

                        txMgr.transactional {
                            repo.updateCourier(existingCourier)
                        }
                    }
                }
            }
        }
    }
}

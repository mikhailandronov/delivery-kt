package org.ama.delivery.infrastructure.tests

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.ama.delivery.core.domain.entities.CourierId
import org.ama.delivery.core.domain.entities.OrderId
import org.ama.delivery.core.ports.outbound.ICourierRepository
import org.ama.delivery.core.ports.outbound.IOrderRepository
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.UUID
import kotlin.getValue

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
    }
}

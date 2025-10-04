package org.ama.delivery.infrastructure.tests

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldNotBe
import org.ama.delivery.core.domain.entities.OrderId
import org.ama.delivery.core.ports.outbound.IOrderRepository
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.UUID

class KtormOrderRepositoryTests : BehaviorSpec(), KoinTest {
    init {
        extension(KoinExtension(testModuleInfra))

        context("get order by id") {
            given("a repository object") {
                val repo: IOrderRepository by inject()

                When("request with existing id") {
                    val existingId = OrderId(UUID.fromString("9af55f14-a68b-4947-83f2-84c5c6e584c8"))

                    then("the correct Order object should be returned") {
                        val foundOrder = repo.getOrderById(existingId)
                        foundOrder shouldNotBe null
                        foundOrder?.id() shouldBe existingId
                    }
                }

                When("request with non-existing id") {
                    then("null value should be returned") {
                        val foundOrder = repo.getOrderById(OrderId())
                        foundOrder shouldBe null
                    }
                }
            }
        }
    }
}
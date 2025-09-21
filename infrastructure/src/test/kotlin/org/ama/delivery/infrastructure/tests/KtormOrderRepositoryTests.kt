package org.ama.delivery.infrastructure.tests

import io.kotest.assertions.throwables.shouldThrow
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
                    then("the correct Order object should be returned") {
                        val order = repo.getOrderById(
                            OrderId(UUID.fromString("d0d1f84e-5ed2-412b-bd6e-66f8f2b11c0d"))
                        )
                        order shouldNotBe null
                    }
                }

                When("request with non-existing id") {
                    then("null value should be returned") {

                    }
                }
            }
        }
    }
}
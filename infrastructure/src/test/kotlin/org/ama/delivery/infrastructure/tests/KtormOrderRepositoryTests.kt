package org.ama.delivery.infrastructure.tests

import arrow.core.getOrElse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldNotBe
import org.ama.delivery.core.domain.common.Location
import org.ama.delivery.core.domain.common.Volume
import org.ama.delivery.core.domain.entities.OrderId
import org.ama.delivery.core.domain.entities.OrderStatus
import org.ama.delivery.core.ports.outbound.IOrderRepository
import org.ama.delivery.core.ports.outbound.ITransactionManager
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.UUID

@Suppress("unused")
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

        context("get assigned orders") {
            given("a repository object") {
                val repo: IOrderRepository by inject()

                When("requesting assigned orders") {
                    then("should return orders with Assigned status") {
                        val assignedOrders = repo.getAssignedOrders()
                        
                        assignedOrders shouldNotBe null

                        assignedOrders.forEach { order ->
                            order.status() shouldBe OrderStatus.Assigned
                            order.courierId() shouldNotBe null
                        }
                        
                        assignedOrders.isNotEmpty() shouldBe true
                    }
                }
            }
        }

        context("get random created order") {
            given("a repository object") {
                val repo: IOrderRepository by inject()

                When("requesting random created order") {
                    then("should return an order with Created status or null if none") {
                        val created = repo.getRandomCreatedOrder()

                        if (created != null) {
                            created.status() shouldBe OrderStatus.Created
                            created.courierId() shouldBe null
                        }
                    }
                }
            }
        }

        context("add new order") {
            given("a repository object and transaction manager") {
                val repo: IOrderRepository by inject()
                val txMgr: ITransactionManager by inject()

                When("adding an order outside of transaction") {
                    then("should throw IllegalStateException") {
                        val destination = Location.from(1, 2).getOrElse { error("invalid destination in test") }
                        val volume = Volume.from(3).getOrElse { error("invalid volume in test") }
                        val order = org.ama.delivery.core.domain.entities.Order.create(destination, volume)
                            .getOrElse { error("failed to create order in test") }

                        shouldThrow<IllegalStateException> {
                            repo.addNewOrder(order)
                        }
                    }
                }

                When("adding an order within a transaction") {
                    then("should persist and be retrievable by id") {
                        val destination = Location.from(3, 4).getOrElse { error("invalid destination in test") }
                        val volume = Volume.from(5).getOrElse { error("invalid volume in test") }
                        val newOrder = org.ama.delivery.core.domain.entities.Order.create(destination, volume)
                            .getOrElse { error("failed to create order in test") }

                        txMgr.transactional {
                            repo.addNewOrder(newOrder)
                        }

                        val found = repo.getOrderById(newOrder.id())
                        found shouldNotBe null
                        found?.id() shouldBe newOrder.id()
                        found?.status() shouldBe OrderStatus.Created
                        found?.courierId() shouldBe null
                    }
                }
            }
        }
    }
}
package org.ama.delivery.infrastructure.tests

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.koin.KoinExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.ama.delivery.core.ports.outbound.ITransactionManager
import org.ama.delivery.infrastructure.adapters.relational.KtormTransactionContext

class KtormTransactionManagerTests : BehaviorSpec(), KoinTest {
    init {
        extension(KoinExtension(testModuleInfra))

        val txMgr: ITransactionManager by inject()

        context("run transactional processing block") {
            given("a code block to be run in transaction") {
                val txSum = { a: Int, b: Int ->
                    if (KtormTransactionContext.get() == null)
                        throw IllegalStateException("Operation should be run in transaction")
                    a + b
                }

                When("try to run it outside of transaction") {
                    then("an error should be returned, block not executed") {
                        var sum = 0
                        shouldThrow<IllegalStateException> {
                            sum = txSum(2, 3)
                        }
                        sum shouldBe 0
                    }
                }

                When("try to run it in scope of transaction (commit)") {
                    then("the block is executed and return correct result") {
                        var sum = 0
                        txMgr.transactional {
                            sum = txSum(2, 3)
                        }
                        sum shouldBe 5
                    }
                }

                When("try to run it in scope of transaction (rollback)") {
                    then("the block is executed, correct exception returns") {
                        var sum = 0
                        shouldThrow<ArithmeticException> {
                            txMgr.transactional {
                                sum = 1 / txSum(0, 0)
                            }
                        }
                        sum shouldBe 0
                    }
                }

                When("try to run it outside of transaction (after)") {
                    then("an error should be returned, block not executed") {
                        var sum = 0
                        txMgr.transactional {
                            sum = 5
                        }

                        shouldThrow<IllegalStateException> {
                            sum = txSum(sum, 3)
                        }
                        sum shouldBe 5
                    }
                }
            }
        }
    }
}
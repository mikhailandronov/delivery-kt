package org.ama.delivery.infrastructure.tests

import org.ama.delivery.core.ports.outbound.ITransactionManager
import org.ama.delivery.infrastructure.adapters.relational.H2TransactionManager
import org.koin.dsl.module

val testModuleInfra = module {
    single<ITransactionManager> { H2TransactionManager("jdbc:h2:mem:test") }
}
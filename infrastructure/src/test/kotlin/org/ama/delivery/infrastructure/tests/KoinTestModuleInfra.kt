package org.ama.delivery.infrastructure.tests

import org.ama.delivery.core.ports.outbound.IOrderRepository
import org.ama.delivery.core.ports.outbound.ITransactionManager
import org.ama.delivery.infrastructure.adapters.relational.KtormOrderRepository
import org.ama.delivery.infrastructure.adapters.relational.KtormTransactionManager
import org.koin.dsl.module
import org.ktorm.database.Database

val testModuleInfra = module {
    single<Database> {
//        Database.connect(
//            url = "jdbc:h2:mem:test",
//            driver = "org.h2.Driver"
//        )

        Database.connect (
            url = "jdbc:postgresql://158.160.45.79:5432/delivery",
            driver = "org.postgresql.Driver",
            user = "username",
            password = "secret"
        )
    }

    single<ITransactionManager> { KtormTransactionManager(get ()) }
    single<IOrderRepository> { KtormOrderRepository(get()) }
}
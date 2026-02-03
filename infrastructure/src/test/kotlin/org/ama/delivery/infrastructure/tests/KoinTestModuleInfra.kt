package org.ama.delivery.infrastructure.tests

import org.ama.delivery.core.ports.outbound.ICourierRepository
import org.ama.delivery.core.ports.outbound.IOrderRepository
import org.ama.delivery.core.ports.outbound.ITransactionManager
import org.ama.delivery.infrastructure.adapters.relational.KtormCourierRepository
import org.ama.delivery.infrastructure.adapters.relational.KtormOrderRepository
import org.ama.delivery.infrastructure.adapters.relational.KtormTransactionManager
import org.koin.dsl.module
import org.ktorm.database.Database
import java.lang.System.getenv

val testModuleInfra = module {
    single<Database> {
//        Database.connect(
//            url = "jdbc:h2:mem:test",
//            driver = "org.h2.Driver"
//        )

        Database.connect(
            url = "jdbc:postgresql://localhost:5432/delivery",
            driver = "org.postgresql.Driver",
            user = getenv("DB_USER") ?: throw IllegalStateException("DB_USER environment variable not set"),
            password = getenv("DB_PASSWORD") ?: throw IllegalStateException("DB_PASSWORD environment variable not set")
        )
    }

    single<ITransactionManager> { KtormTransactionManager(get ()) }
    single<IOrderRepository> { KtormOrderRepository(get()) }
    single<ICourierRepository> { KtormCourierRepository(get()) }
}
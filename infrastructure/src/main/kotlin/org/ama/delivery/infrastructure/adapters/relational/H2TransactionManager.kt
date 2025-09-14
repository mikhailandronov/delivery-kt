package org.ama.delivery.infrastructure.adapters.relational

import org.ama.delivery.core.ports.outbound.ITransactionManager
import org.ktorm.database.Database

class H2TransactionManager(url: String) : ITransactionManager
by KtormTransactionManager(
    Database.connect(
        url,
        "org.h2.Driver"
    )
)
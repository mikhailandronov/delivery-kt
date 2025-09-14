package org.ama.delivery.infrastructure.adapters.relational

import org.ama.delivery.core.ports.outbound.ITransactionManager
import org.ktorm.database.Database

class PostgresTransactionManager (url: String, user: String, password: String) : ITransactionManager
by KtormTransactionManager(
    Database.connect(
        url,
        "org.postgresql.Driver",
        user, password
    )
)
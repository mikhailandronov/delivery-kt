package org.ama.delivery.core.ports.outbound

interface ITransactionManager {
    fun <T> transactional(block: () -> T): T
}
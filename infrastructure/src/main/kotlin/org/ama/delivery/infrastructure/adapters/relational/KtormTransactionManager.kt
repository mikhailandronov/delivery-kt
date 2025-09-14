package org.ama.delivery.infrastructure.adapters.relational

import org.ama.delivery.core.ports.outbound.ITransactionManager
import org.ktorm.database.Database
import org.ktorm.database.Transaction

// Общая реализация TransactionManager для всех СУБД, с которыми работает Ktorm
class KtormTransactionManager(private val database: Database): ITransactionManager {
    override fun <T> transactional(block: () -> T): T {
        // Если транзакция уже начата, просто выполняем блок в ее рамках
        if (KtormTransactionContext.get() != null) {
            return block()
        }

        // Иначе, начинаем новую транзакцию
        val transaction = database.transactionManager.newTransaction()
        KtormTransactionContext.set(transaction)
        println("--- Транзакция НАЧАТА ---")

        try {
            val result = block()
            transaction.commit()
            println("--- Транзакция ЗАВЕРШЕНА (COMMIT) ---")
            return result
        } catch (e: Exception) {
            transaction.rollback()
            println("--- Транзакция ОТКАТИЛАСЬ (ROLLBACK) ---")
            throw e
        } finally {
            KtormTransactionContext.clear()
            transaction.close()
        }
    }
}

object KtormTransactionContext {
    private val currentTransaction = ThreadLocal<Transaction>()

    fun get(): Transaction? = currentTransaction.get()
    fun set(tx: Transaction) = currentTransaction.set(tx)
    fun clear() = currentTransaction.remove()
}
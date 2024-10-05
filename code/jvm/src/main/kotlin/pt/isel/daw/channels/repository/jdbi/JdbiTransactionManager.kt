package pt.isel.daw.channels.repository.jdbi

import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pt.isel.daw.channels.repository.Transaction
import pt.isel.daw.channels.repository.TransactionManager

@Component
class JdbiTransactionManager(
    private val jdbi: Jdbi
): TransactionManager {
    private val logger = LoggerFactory.getLogger(JdbiTransactionManager::class.java)

    override fun <R> run(block: (Transaction) -> R): R {
        return try {
            jdbi.inTransaction<R, Exception> { handle ->
                val transaction = JdbiTransaction(handle)
                block(transaction)
            }
        } catch (e: Exception) {
            logger.error("Transaction failed", e)
            throw e
        }
    }
}
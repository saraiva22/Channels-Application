package pt.isel.daw.channels.repository

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}

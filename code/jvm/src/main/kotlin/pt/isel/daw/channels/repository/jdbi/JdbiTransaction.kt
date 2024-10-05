package pt.isel.daw.channels.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.isel.daw.channels.repository.ChannelsRepository
import pt.isel.daw.channels.repository.Transaction

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val channelsRepository: ChannelsRepository = JdbiChannelsRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}
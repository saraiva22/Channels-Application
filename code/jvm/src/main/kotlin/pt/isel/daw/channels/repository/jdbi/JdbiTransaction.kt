package pt.isel.daw.channels.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.isel.daw.channels.repository.ChannelsRepository
import pt.isel.daw.channels.repository.MessagesRepository
import pt.isel.daw.channels.repository.Transaction
import pt.isel.daw.channels.repository.UsersRepository

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val channelsRepository: ChannelsRepository = JdbiChannelsRepository(handle)
    override val usersRepository: UsersRepository = JdbiUsersRepository(handle)
    override val messagesRepository: MessagesRepository = JdbiMessageRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}
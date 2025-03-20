package pt.isel.daw.channels.repository

interface Transaction {

    val channelsRepository: ChannelsRepository
    val usersRepository: UsersRepository
    val messagesRepository: MessagesRepository

    // other repository types
    fun rollback()
}
package pt.isel.daw.channels.repository

interface Transaction {

    val channelsRepository: ChannelsRepository
    val usersRepository: UsersRepository

    // other repository types
    fun rollback()
}
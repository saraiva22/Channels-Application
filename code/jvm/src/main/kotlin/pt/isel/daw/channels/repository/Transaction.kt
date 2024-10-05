package pt.isel.daw.channels.repository

interface Transaction {

    val channelsRepository: ChannelsRepository

    // other repository types
    fun rollback()
}
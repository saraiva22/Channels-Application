package pt.isel.daw.channels.http.model.channel

import pt.isel.daw.channels.domain.channels.Channel

data class ChannelsListOutputModel (
    val channels: List<Channel>
)
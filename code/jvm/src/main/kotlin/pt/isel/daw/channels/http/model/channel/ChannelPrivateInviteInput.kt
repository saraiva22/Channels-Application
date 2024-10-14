package pt.isel.daw.channels.http.model.channel

import pt.isel.daw.channels.domain.channels.Privacy

data class ChannelPrivateInviteInput(
    val privacy: Privacy,
    val username: String
)

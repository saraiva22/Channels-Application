package pt.isel.daw.channels.domain.channels

import pt.isel.daw.channels.domain.user.User

data class ChannelModel (
    val name: String,
    val owner: User,
    val rules: String,
    val type: Type
)
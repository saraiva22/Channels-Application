package pt.isel.daw.channels.http.model.channel

import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.domain.user.User

data class ChannelOutputModel (
    val id: Int,
    val name: String,
    val owner: User,
    val members: List<User>
)
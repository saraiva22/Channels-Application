package pt.isel.daw.channels.http.model

import pt.isel.daw.channels.domain.user.User

class ChannelOutputModel(
    val id: Int,
    val name: String,
    val owner: User,
    val members: List<User>
) {
}
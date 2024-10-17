package pt.isel.daw.channels.domain.channels

import pt.isel.daw.channels.domain.user.User

data class Channel (
    val id: Int,
    val name: String,
    val owner: User,
    val members: List<User>
)
package pt.isel.daw.channels.domain.channels

import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.domain.user.UserInfo

data class Channel (
    val id: Int,
    val name: String,
    val owner: UserInfo,
    val members: List<UserInfo>
)
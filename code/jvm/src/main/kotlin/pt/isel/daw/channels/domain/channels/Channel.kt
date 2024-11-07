package pt.isel.daw.channels.domain.channels

import pt.isel.daw.channels.domain.user.UserInfo

data class Channel (
    val id: Int,
    val name: String,
    val owner: UserInfo,
    val type: Type,
    val members: List<UserInfo>
)
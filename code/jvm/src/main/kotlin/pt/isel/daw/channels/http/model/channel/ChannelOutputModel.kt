package pt.isel.daw.channels.http.model.channel

import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.domain.user.UserInfo

data class ChannelOutputModel (
    val id: Int,
    val name: String,
    val owner: UserInfo,
    val type: Type,
    val members: List<UserInfo>,
    val bannedMembers: List<UserInfo>
)
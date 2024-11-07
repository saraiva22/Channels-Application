package pt.isel.daw.channels.http.model.channel

import org.jdbi.v3.core.mapper.Nested
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.domain.user.UserInfo

data class ChannelDbModel (
    val id: Int,
    val name: String,
    @Nested("owner")
    val owner: UserInfo,
    val type: Int,
    val members: List<UserInfo> = emptyList()
) {
    fun toChannel() =
            Channel(
                id,
                name,
                owner,
                Type.fromDBInt(type),
                members
            )
}
package pt.isel.daw.channels.http.model.channel

import org.jdbi.v3.core.mapper.Nested
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.user.User

data class ChannelDbModel (
    val id: Int,
    val name: String,
    @Nested("owner")
    val owner: User,
    val members: List<User> = emptyList()
) {
    fun toChannel() = Channel(id, name, owner, members)
}
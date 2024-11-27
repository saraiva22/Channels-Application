package pt.isel.daw.channels.http.model.message

import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.Nested
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.messages.Message
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.domain.user.UserInfo

data class MessageDbModel (
    val id: Int,
    val text: String,
    @Nested("user")
    val user: UserInfo,
    val created: Long
) {
    fun toMessage(channel: Channel) =
        Message(id, text, channel, user, Instant.fromEpochSeconds(created))
}
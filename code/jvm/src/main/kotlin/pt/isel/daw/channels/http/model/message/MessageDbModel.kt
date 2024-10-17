package pt.isel.daw.channels.http.model.message

import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.Nested
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.messages.Message
import pt.isel.daw.channels.domain.user.User

data class MessageDbModel (
    val id: Int,
    val text: String,
    @Nested("channel")
    val channel: Channel,
    @Nested("user")
    val user: User,
    val created: Long
) {
    fun toMessage() = Message(id, text, channel, user, Instant.fromEpochSeconds(created))
}
package pt.isel.daw.channels.domain.messages

import kotlinx.datetime.Instant
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.user.User

data class Message (
    val id: Int,
    val text: String,
    val channel: Channel,
    val user: User,
    val created: Instant
)
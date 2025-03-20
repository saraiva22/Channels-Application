package pt.isel.daw.channels.domain.messages

import kotlinx.datetime.Instant
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.user.UserInfo


/**
 * Represents a message in a channel
 * @property id the message id
 * @property text the message text
 * @property channel the channel where the message was sent
 * @property user the user that sent the message
 * @property created the date and time when the message was sent
 *
 */
data class Message (
    val id: Int,
    val text: String,
    val channel: Channel,
    val user: UserInfo,
    val created: Instant
)
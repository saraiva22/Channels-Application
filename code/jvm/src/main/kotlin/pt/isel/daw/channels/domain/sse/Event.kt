package pt.isel.daw.channels.domain.sse

import kotlinx.datetime.Instant
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.channels.Status
import pt.isel.daw.channels.domain.user.UserInfo


sealed interface Event {
    data class Message(
        val id: Long,
        val messageId: Int,
        val text: String,
        val channel: Channel,
        val user: UserInfo,
        val created: String
    ) : Event

    data class Invite(
        val id: Long,
        val codHash : String,
        val privacy : Privacy,
        val status : Status,
        val user: UserInfo,
        val channelId : Int,
        val channelName : String,
    ) : Event

    data class KeepAlive(val timestamp: Instant) : Event
}


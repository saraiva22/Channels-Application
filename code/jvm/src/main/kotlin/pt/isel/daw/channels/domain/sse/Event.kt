package pt.isel.daw.channels.domain.sse

import kotlinx.datetime.Instant


sealed interface Event {
    data class Message(val id: Long, val messageId: Int, val channelId : Int, val username: String, val msg: String, val created: String) : Event

    data class KeepAlive(val timestamp: Instant) : Event
}


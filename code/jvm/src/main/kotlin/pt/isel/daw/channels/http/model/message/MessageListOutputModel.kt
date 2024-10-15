package pt.isel.daw.channels.http.model.message

import pt.isel.daw.channels.domain.messages.Message

data class MessageListOutputModel (
    val messages: List<Message>
)
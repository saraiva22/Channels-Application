package pt.isel.daw.channels.domain.messages

data class MessageModel (
    val text: String,
    val channel: Int,
    val user: Int
)
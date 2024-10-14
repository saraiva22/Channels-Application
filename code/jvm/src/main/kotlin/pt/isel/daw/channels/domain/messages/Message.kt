package pt.isel.daw.channels.domain.messages

data class Message (
    val id: Int,
    val text: String,
    val channel: Int,
    val user: Int
)
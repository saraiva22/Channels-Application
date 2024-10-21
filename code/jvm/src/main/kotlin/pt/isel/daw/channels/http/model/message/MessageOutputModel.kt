package pt.isel.daw.channels.http.model.message

import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.domain.user.UserInfo

data class MessageOutputModel (
    val id: Int,
    val text: String,
    val channel: Channel,
    val user: UserInfo,
    val created: String
)
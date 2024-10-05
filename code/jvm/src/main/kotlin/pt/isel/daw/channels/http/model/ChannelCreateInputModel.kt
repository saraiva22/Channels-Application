package pt.isel.daw.channels.http.model

import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.domain.user.User

data class ChannelCreateInputModel (
    val name: String,
    val owner: User,
    val rules: String,
    val type: Type
)
package pt.isel.daw.channels.http.model

import pt.isel.daw.channels.domain.channels.Type

data class ChannelCreateInputModel (
    val name: String,
    val owner: Int,
    val rules: String,
    val type: Type
)
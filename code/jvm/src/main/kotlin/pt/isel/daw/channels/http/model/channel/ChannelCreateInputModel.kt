package pt.isel.daw.channels.http.model.channel

import pt.isel.daw.channels.domain.channels.Type

data class ChannelCreateInputModel (
    val name: String,
    val type: Type
)
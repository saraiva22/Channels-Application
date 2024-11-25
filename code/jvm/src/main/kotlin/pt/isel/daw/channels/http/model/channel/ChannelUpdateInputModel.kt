package pt.isel.daw.channels.http.model.channel

import pt.isel.daw.channels.domain.channels.Type

data class ChannelUpdateInputModel(
    val name: String? = null,
    val type: Type? = null
)

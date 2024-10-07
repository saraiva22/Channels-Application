package pt.isel.daw.channels.http.model.channel

import pt.isel.daw.channels.domain.channels.Type

data class ChannelOutputModel (
    val id: Int,
    val name: String,
    val owner: Int,
    val rules: String,
    val type: Type,
    val members: List<Int>
)
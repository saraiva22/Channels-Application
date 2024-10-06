package pt.isel.daw.channels.domain.channels

data class ChannelModel (
    val name: String,
    val owner: Int,
    val rules: String,
    val type: Type
)
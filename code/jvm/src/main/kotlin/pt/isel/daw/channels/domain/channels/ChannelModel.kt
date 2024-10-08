package pt.isel.daw.channels.domain.channels

data class ChannelModel (
    val name: String,
    val owner: Int,
    val type: Type
) {
    init {
        require(owner > 0)
    }
}
package pt.isel.daw.channels.domain.channels


/**
 * Represent a ChannelModel
 * @property name The name of the channel
 * @property owner The owner of the channel
 * @property type The type of the channel
 */

data class ChannelModel (
    val name: String,
    val owner: Int,
    val type: Type
) {
    init {
        require(owner > 0)
    }
}
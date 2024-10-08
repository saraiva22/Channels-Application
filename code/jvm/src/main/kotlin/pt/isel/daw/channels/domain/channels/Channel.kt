package pt.isel.daw.channels.domain.channels

data class Channel (
    val id: Int,
    val name: String,
    val owner: Int,
    val members: List<Int>
) {
    init {
        require(id > 0)
        require(owner > 0)
    }
}
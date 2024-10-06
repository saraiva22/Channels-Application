package pt.isel.daw.channels.domain.channels

data class Channel (
    val id: Int,
    val name: String,
    val owner: Int,
    val rules: String,
    val members: List<Int>
)
package pt.isel.daw.channels.domain.channels

enum class State {
    UNBANNED,
    BANNED;

    companion object {
        fun fromInt(value: Int): State {
            return entries[value]
        }
    }
}
package pt.isel.daw.channels.domain.channels

enum class Type {
    PUBLIC,
    PRIVATE;

    companion object {
        fun fromDBInt(value: Int): Type {
            return entries[value]
        }
    }
}
package pt.isel.daw.channels.domain.channels

/**
 * Enum class for Type
 * PUBLIC(0) - Represents a public channel
 * PRIVATE(1) - Represents a private channel
 */
enum class Type {
    PUBLIC,
    PRIVATE;

    companion object {
        fun fromInt(value: Int): Type {
            return entries[value]
        }
    }
}
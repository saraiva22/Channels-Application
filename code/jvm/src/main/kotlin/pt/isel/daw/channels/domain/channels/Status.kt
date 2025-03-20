package pt.isel.daw.channels.domain.channels

/**
 * Enum class for Status
 * PENDING - Represents a pending status
 * ACCEPT - Represents a accept status
 * REJECT - Represents a reject status
 */
enum class Status {
    PENDING,
    ACCEPT,
    REJECT;

    companion object {
        fun fromInt(value: Int): Status {
            return Status.entries[value]
        }
    }
}

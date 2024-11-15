package pt.isel.daw.channels.domain.channels


/**
 * Enum class for Privacy
 * READ_ONLY(0) - Represents a read only channel
 * READ_WRITE(1) - Represents a read write channel
 */
enum class Privacy {
    READ_ONLY, READ_WRITE;

    companion object {
        fun fromInt(value: Int): Privacy {
            return entries[value]
        }
    }
}

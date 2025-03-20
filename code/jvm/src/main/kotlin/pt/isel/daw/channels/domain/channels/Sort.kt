package pt.isel.daw.channels.domain.channels

/**
 * Enum class for Sort
 * NAME - Represents a sort by name
 */
enum class Sort(val value: String) {
    NAME("name");

    companion object {
        fun fromString(value: String): Sort? {
            return entries.find { it.value == value.lowercase() }
        }
    }
}
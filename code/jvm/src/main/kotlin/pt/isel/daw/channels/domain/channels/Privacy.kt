package pt.isel.daw.channels.domain.channels

enum class Privacy(private val value: Int) {
    READ_ONLY(0), READ_WRITE(1);


    companion object {
        fun fromDBInt(value: Int): Privacy {
            return entries[value]
        }
    }
}

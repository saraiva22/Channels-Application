package pt.isel.daw.channels.domain.user.components

class Email private constructor(
    val value: String
){

    companion object{
        private const val EMAIL_REGEX = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$"
    }
}
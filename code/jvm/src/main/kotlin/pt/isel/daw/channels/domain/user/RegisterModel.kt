package pt.isel.daw.channels.domain.user

data class RegisterModel (
    val userId: Int,
    val codHash: String,
    val expired: Boolean
)
package pt.isel.daw.channels.http.model.user

data class RegisterModel (
    val userId: Int,
    val codHash: String,
    val expired: Boolean
)
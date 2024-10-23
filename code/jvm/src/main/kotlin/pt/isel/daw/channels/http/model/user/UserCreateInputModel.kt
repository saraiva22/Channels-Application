package pt.isel.daw.channels.http.model.user

data class UserCreateInputModel(
    val username: String,
    val email: String,
    val password: String,
    val inviteCode: String?
)
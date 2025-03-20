package pt.isel.daw.channels.http.model.user

data class UserCreateTokenInputModel(
    val username: String,
    val password: String,
)
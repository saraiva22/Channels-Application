package pt.isel.daw.channels.domain.user

class AuthenticatedUser(
    val user: User,
    val token: String,
)
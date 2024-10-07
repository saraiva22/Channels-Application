package pt.isel.daw.channels.domain.user

import pt.isel.daw.channels.domain.user.components.Email
import pt.isel.daw.channels.domain.user.components.Username

data class User(
    val id: UInt,
    val email: String,
    val username: String,
    val passwordValidation: PasswordValidationInfo,
)

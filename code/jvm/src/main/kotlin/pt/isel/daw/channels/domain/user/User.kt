package pt.isel.daw.channels.domain.user

import pt.isel.daw.channels.domain.user.components.Email
import pt.isel.daw.channels.domain.user.components.Username

data class User(
    val id: UInt,
    val email: Email,
    val username: Username,
    val passwordValidation: PasswordValidationInfo,
)

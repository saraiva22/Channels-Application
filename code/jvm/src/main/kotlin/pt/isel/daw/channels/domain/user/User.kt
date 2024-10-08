package pt.isel.daw.channels.domain.user

import pt.isel.daw.channels.domain.user.components.Email
import pt.isel.daw.channels.domain.user.components.Username

data class User(
    val id: Int,
    val email: String,
    val username: String,
    val passwordValidation: PasswordValidationInfo,
) {
    init {
        require(id > 0)
    }
}

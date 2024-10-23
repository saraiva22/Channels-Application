package pt.isel.daw.channels.domain.user

data class User(
    val id: Int,
    val email: String,
    val username: String,
    val passwordValidation: PasswordValidationInfo,
) {
    init {
        require(id > 0)
        require(email.isNotBlank())
        require(username.isNotBlank())


    }

}

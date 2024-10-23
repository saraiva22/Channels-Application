package pt.isel.daw.channels.http.model.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class UserCreateInputModel(
    @field:NotBlank(message = "Username must not be blank")
    @field:Size(min = 5, max = 30, message = "Username must have between 5 and 30 characters")
    val username: String,
    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be valid")
    val email: String,
    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 5, max = 40, message = "Password must have between 8 and 40 characters")
    val password: String,
    val inviteCode: String?
)
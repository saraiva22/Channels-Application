package pt.isel.daw.channels.http.model

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE
import java.net.URI

class Problem(
    val typeUri: URI,
    val title: String,
    val status: Int,
    val detail: String,
    val instance: URI? = null
) {
    fun toResponse() = ResponseEntity
        .status(status)
        .header("Content-Type", MEDIA_TYPE)
        .body<Any>(this)

    companion object {
        const val MEDIA_TYPE = APPLICATION_PROBLEM_JSON_VALUE
        const val BASE_URL = "https://github.com/isel-leic-daw/2024-daw-leic51d-g10-1/blob/main/docs/problems/"


        //Default
        val internalServerError = URI("${BASE_URL}internal-server-error")

        // User
        private val usernameAlreadyExists = URI("${BASE_URL}username-already-exists")
        private val emailAlreadyExists = URI("${BASE_URL}email-already-exists")
        private val userNotFound = URI("${BASE_URL}user-not-found")
        private val invalidEmail = URI("${BASE_URL}invalid-email")
        private val insecurePassword = URI("${BASE_URL}insecure-password")
        private val userOrPasswordAreInvalid = URI("${BASE_URL}user-or-password-are-invalid")


        //Token
        private val invalidToken = URI("${BASE_URL}user-not-found")
        private val tokenNotRevoked = URI("${BASE_URL}token-not-revoked")
        val unauthorized = URI("${BASE_URL}unauthorized")


        // Channel
        private val channelNotFound = URI("${BASE_URL}channel-not-found")
        private val channelAlreadyExists = URI("${BASE_URL}channel-already-exists")
        val invalidChannelType = URI("${BASE_URL}invalid-channel-type")

        fun internalServerError(
            instance: URI?
        ): ResponseEntity<*> = Problem(
            typeUri = internalServerError,
            title = "Internal server error",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            detail = "An internal server error occurred",
            instance = instance
        ).toResponse()


        fun usernameAlreadyExists(username: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = usernameAlreadyExists,
            title = "UserName already exists",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Give username $username already exists",
            instance = instance
        ).toResponse()

        fun emailAlreadyExists(email: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = emailAlreadyExists,
            title = "Email already exists",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Give emails $email already exists",
            instance = instance
        ).toResponse()


        fun userNotFound(id: Int, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = userNotFound,
            title = "User not found",
            status = HttpStatus.NOT_FOUND.value(),
            detail = "User with given id: $id not found",
            instance = instance
        ).toResponse()

        fun invalidEmail(email: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = invalidEmail,
            title = "Invalid email",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Email is invalid",
            instance = instance
        ).toResponse()

        fun channelAlreadyExists(instance: URI?): ResponseEntity<*> = Problem(
            typeUri = channelAlreadyExists,
            title = "Channel already exists",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Channel already exists",
            instance = instance
        ).toResponse()

        fun invalidChannelType(instance: URI?): ResponseEntity<*> = Problem(
            typeUri = invalidChannelType,
            title = "Invalid channel type",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Invalid channel type",
            instance = instance
        ).toResponse()

        fun channelNotFound(instance: URI?): ResponseEntity<*> = Problem(
            typeUri = channelNotFound,
            title = "Channel not found",
            status = HttpStatus.NOT_FOUND.value(),
            detail = "Channel not found",
            instance = instance
        ).toResponse()

        fun insecurePassword(instance: URI?): ResponseEntity<*> = Problem(
            typeUri = insecurePassword,
            title = "Insecure password",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Password is insecure",
            instance = instance
        ).toResponse()

        fun invalidToken(instance: URI?): ResponseEntity<*> = Problem(
            typeUri = invalidToken,
            title = "Invalid token",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Invalid token",
            instance = instance
        ).toResponse()

        fun tokenNotRevoked(instance: URI?): ResponseEntity<*> = Problem(
            typeUri = tokenNotRevoked,
            title = "Token not revoked",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Token not revoked",
            instance = instance
        ).toResponse()

        fun userOrPasswordAreInvalid(instance: URI?): ResponseEntity<*> = Problem(
            typeUri = userOrPasswordAreInvalid,
            title = "User or password are invalid",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "User or password are invalid",
            instance = instance
        ).toResponse()

        fun unauthorized(instance: URI?): ResponseEntity<*> = Problem(
            typeUri = unauthorized,
            title = "Unauthorized",
            status = HttpStatus.UNAUTHORIZED.value(),
            detail = "The request has not been applied because it lacks valid authentication credentials for the target resource.",
            instance = instance
        ).toResponse()


    }
}
package pt.isel.daw.channels.http.media

import org.springframework.http.HttpStatus
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

        fun response(status: Int, problem: Problem) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problem)

        const val MEDIA_TYPE = APPLICATION_PROBLEM_JSON_VALUE
        private const val BASE_URL = "https://github.com/isel-leic-daw/2024-daw-leic51d-g10-1/blob/main/docs/problems/"


        //Default
        val internalServerError = URI("${BASE_URL}internal-server-error")
        val badRequest = URI("${BASE_URL}bad-request")
        val invalidRequestContent = URI("${BASE_URL}invalid-request-content")

        // User
        private val usernameAlreadyExists = URI("${BASE_URL}username-already-exists")
        private val emailAlreadyExists = URI("${BASE_URL}email-already-exists")
        private val invalidInviteCode = URI("${BASE_URL}invalid-register-code")
        private val userNotFound = URI("${BASE_URL}user-not-found")
        private val usernameNotFound = URI("${BASE_URL}username-not-found")
        private val invalidEmail = URI("${BASE_URL}invalid-email")
        private val insecurePassword = URI("${BASE_URL}insecure-password")
        private val userOrPasswordAreInvalid = URI("${BASE_URL}user-or-password-are-invalid")


        //Token
        private val invalidToken = URI("${BASE_URL}user-not-found")
        private val tokenNotRevoked = URI("${BASE_URL}token-not-revoked")
        val unauthorized = URI("${BASE_URL}unauthorized")


        // Channel
        private val channelNotFound = URI("${BASE_URL}channel-not-found")
        private val channelNameNotFound = URI("${BASE_URL}channel-name-not-found")
        private val channelAlreadyExists = URI("${BASE_URL}channel-already-exists")
        private val invalidChannelType = URI("${BASE_URL}invalid-channel-type")
        private val userNotInChannel = URI("${BASE_URL}user-not-in-channel")
        private val userAlreadyInChannel = URI("${BASE_URL}user-already-in-channel")
        private val channelNameAlreadyExists = URI("${BASE_URL}channel-name-already-exists")
        private val userNotPermissionsType = URI("${BASE_URL}user-not-permissions-type")
        private val codeInvalidOrExpiredChannel = URI("${BASE_URL}code-invalid-or-expired-channel")
        private val isPrivateChannel = URI("${BASE_URL}is-private-channel")
        private val channelIsPublic = URI("${BASE_URL}channel-is-public")
        private val privacyTypeInvalid = URI("${BASE_URL}privacy-type-invalid")
        private val errorLeavingChannel = URI("${BASE_URL}error-leaving-channel")

        // Message
        private val messageNotFound = URI("${BASE_URL}message-not-found")
        private val userPrivacyTypeReadOnly = URI("${BASE_URL}user-privacy-type-read-only")

        fun internalServerError(
            instance: URI?
        ): ResponseEntity<*> = Problem(
            typeUri = internalServerError,
            title = "Internal server error",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            detail = "An internal server error occurred",
            instance = instance
        ).toResponse()


        fun invalidRequestContent(): Problem = Problem(
            typeUri = invalidRequestContent,
            title = "Invalid request content",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "The request content is invalid",
            instance = invalidRequestContent
        )


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

        fun usernameNotFound(username: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = usernameNotFound,
            title = "Username not found",
            status = HttpStatus.NOT_FOUND.value(),
            detail = "Username $username not found",
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

        fun channelNotFound(id: Int, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = channelNotFound,
            title = "Channel not found",
            status = HttpStatus.NOT_FOUND.value(),
            detail = "Channel $id not found",
            instance = instance
        ).toResponse()

        fun channelNameNotFound(name: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = channelNotFound,
            title = "Channel not found",
            status = HttpStatus.NOT_FOUND.value(),
            detail = "Channel $name not found",
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

        fun userNotInChannel(username: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = userNotInChannel,
            title = "User not in channel",
            status = HttpStatus.NOT_FOUND.value(),
            detail = "User $username not in channel",
            instance = instance
        ).toResponse()

        fun channelNameAlreadyExists(name: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = channelNameAlreadyExists,
            title = "Channel name already exists",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Channel name $name already exists",
            instance = instance
        ).toResponse()

        fun badRequest(instance: URI?): ResponseEntity<*> = Problem(
            typeUri = badRequest,
            title = "Bad request",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "The request could not be understood by the server due to malformed syntax",
            instance = instance
        ).toResponse()

        fun userAlreadyInChannel(username: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = userAlreadyInChannel,
            title = "User already in channel",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "User $username already in channel",
            instance = instance
        ).toResponse()

        fun invalidInviteRegister(code: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = invalidInviteCode,
            title = "Invitation code is invalid",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Invitation code $code is invalid",
            instance = instance
        ).toResponse()

        fun userNotPermissionsType(username: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = userNotPermissionsType,
            title = "User not permissions type",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "User $username not permissions type",
            instance = instance
        ).toResponse()

        fun codeInvalidOrExpiredChannel(code: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = codeInvalidOrExpiredChannel,
            title = "Code invalid channel",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Code $code is invalid or expired",
            instance = instance
        ).toResponse()

        fun isPrivateChannel(channelId: Int, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = isPrivateChannel,
            title = "Is private channel",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Channel $channelId is private",
            instance = instance
        ).toResponse()

        fun channelIsPublic(channelId: Int, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = channelIsPublic,
            title = "Channel is public",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Channel $channelId is public",
            instance = instance
        ).toResponse()

        fun privacyTypeInvalid(privacy: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = privacyTypeInvalid,
            title = "Privacy type invalid",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Privacy type $privacy invalid",
            instance = instance
        ).toResponse()

        fun userPrivacyTypeReadOnly(username: String, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = userPrivacyTypeReadOnly,
            title = "User privacy type read only",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "User $username privacy type read only",
            instance = instance
        ).toResponse()

        fun errorLeavingChannel(channelId: Int, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = errorLeavingChannel,
            title = "Error leaving channel",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Error leaving channel $channelId",
            instance = instance
        ).toResponse()

        fun messageNotFound(messageId: Int, instance: URI?): ResponseEntity<*> = Problem(
            typeUri = messageNotFound,
            title = "Message not found",
            status = HttpStatus.NOT_FOUND.value(),
            detail = "Message with id $messageId not found",
            instance = instance
        ).toResponse()
    }
}
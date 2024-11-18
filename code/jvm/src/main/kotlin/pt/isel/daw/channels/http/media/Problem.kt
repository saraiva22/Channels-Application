package pt.isel.daw.channels.http.media

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE
import java.net.URI

/**
 *  Represents a problem in the API
 *  @param type: URI of the problem
 *  @param title: Title of the problem
 *  @param status: Status of the problem
 *  @param detail: Detail of the problem
 *  @param instance: Instance of the problem
 */
class Problem(
    val type: URI,
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
        private const val DEFAULT_FOLDER = BASE_URL + "default/"
        private const val USER_FOLDER = BASE_URL + "user/"
        private const val TOKEN_FOLDER = BASE_URL + "token/"
        private const val CHANNEL_FOLDER = BASE_URL + "channel/"
        private const val MESSAGE_FOLDER = BASE_URL + "message/"

        //Default
        val internalServerError = URI("${DEFAULT_FOLDER}internal-server-error")
        val badRequest = URI("${DEFAULT_FOLDER}bad-request")
        val invalidRequestContent = URI("${DEFAULT_FOLDER}invalid-request-content")

        // User
        private val usernameAlreadyExists = URI("${USER_FOLDER}username-already-exists")
        private val emailAlreadyExists = URI("${USER_FOLDER}email-already-exists")
        private val invalidInviteCode = URI("${USER_FOLDER}invalid-register-code")
        private val userNotFound = URI("${USER_FOLDER}user-not-found")
        private val usernameNotFound = URI("${USER_FOLDER}username-not-found")
        private val invalidEmail = URI("${USER_FOLDER}invalid-email")
        private val insecurePassword = URI("${USER_FOLDER}insecure-password")
        private val userOrPasswordAreInvalid = URI("${USER_FOLDER}user-or-password-are-invalid")

        //Token
        private val invalidToken = URI("${TOKEN_FOLDER}user-not-found")
        private val tokenNotRevoked = URI("${TOKEN_FOLDER}token-not-revoked")
        val unauthorized = URI("${TOKEN_FOLDER}unauthorized")

        // Channel
        private val channelNotFound = URI("${CHANNEL_FOLDER}channel-not-found")
        private val channelAlreadyExists = URI("${CHANNEL_FOLDER}channel-already-exists")
        private val invalidChannelType = URI("${CHANNEL_FOLDER}invalid-channel-type")
        private val userNotInChannel = URI("${CHANNEL_FOLDER}user-not-in-channel")
        private val userIsNotChannelOwner = URI("${CHANNEL_FOLDER}user-is-not-channel-owner")
        private val userAlreadyInChannel = URI("${CHANNEL_FOLDER}user-already-in-channel")
        private val channelNameAlreadyExists = URI("${CHANNEL_FOLDER}channel-name-already-exists")
        private val userPermissionsDeniedType = URI("${CHANNEL_FOLDER}user-permissions-denied-type")
        private val codeInvalidOrExpiredChannel = URI("${CHANNEL_FOLDER}code-invalid-or-expired-channel")
        private val channelIsPrivate = URI("${CHANNEL_FOLDER}channel-is-private")
        private val channelIsPublic = URI("${CHANNEL_FOLDER}channel-is-public")
        private val privacyTypeInvalid = URI("${CHANNEL_FOLDER}privacy-type-invalid")
        private val errorLeavingChannel = URI("${CHANNEL_FOLDER}error-leaving-channel")

        // Message
        private val messageNotFound = URI("${MESSAGE_FOLDER}message-not-found")
        private val userPrivacyTypeReadOnly = URI("${MESSAGE_FOLDER}user-privacy-type-read-only")

        fun internalServerError(
            instance: URI?
        ): ResponseEntity<*> = Problem(
            type = internalServerError,
            title = "Internal server error",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            detail = "An internal server error occurred",
            instance = instance
        ).toResponse()


        fun invalidRequestContent(): Problem = Problem(
            type = invalidRequestContent,
            title = "Invalid request content",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "The request content is invalid",
            instance = invalidRequestContent
        )


        fun usernameAlreadyExists(username: String, instance: URI?): ResponseEntity<*> = Problem(
            type = usernameAlreadyExists,
            title = "UserName already exists",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Give username $username already exists",
            instance = instance
        ).toResponse()

        fun emailAlreadyExists(email: String, instance: URI?): ResponseEntity<*> = Problem(
            type = emailAlreadyExists,
            title = "Email already exists",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Give emails $email already exists",
            instance = instance
        ).toResponse()


        fun userNotFound(id: Int, instance: URI?): ResponseEntity<*> = Problem(
            type = userNotFound,
            title = "User not found",
            status = HttpStatus.NOT_FOUND.value(),
            detail = "User with given id: $id not found",
            instance = instance
        ).toResponse()

        fun usernameNotFound(username: String, instance: URI?): ResponseEntity<*> = Problem(
            type = usernameNotFound,
            title = "Username not found",
            status = HttpStatus.NOT_FOUND.value(),
            detail = "Username $username not found",
            instance = instance
        ).toResponse()


        fun invalidEmail(email: String, instance: URI?): ResponseEntity<*> = Problem(
            type = invalidEmail,
            title = "Invalid email",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Email is invalid",
            instance = instance
        ).toResponse()

        fun channelAlreadyExists(instance: URI?): ResponseEntity<*> = Problem(
            type = channelAlreadyExists,
            title = "Channel already exists",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Channel already exists",
            instance = instance
        ).toResponse()

        fun invalidChannelType(instance: URI?): ResponseEntity<*> = Problem(
            type = invalidChannelType,
            title = "Invalid channel type",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Invalid channel type",
            instance = instance
        ).toResponse()

        fun channelNotFound(id: Int, instance: URI?): ResponseEntity<*> = Problem(
            type = channelNotFound,
            title = "Channel not found",
            status = HttpStatus.NOT_FOUND.value(),
            detail = "Channel $id not found",
            instance = instance
        ).toResponse()

        fun insecurePassword(instance: URI?): ResponseEntity<*> = Problem(
            type = insecurePassword,
            title = "Insecure password",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Password is insecure",
            instance = instance
        ).toResponse()

        fun invalidToken(instance: URI?): ResponseEntity<*> = Problem(
            type = invalidToken,
            title = "Invalid token",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Invalid token",
            instance = instance
        ).toResponse()

        fun tokenNotRevoked(instance: URI?): ResponseEntity<*> = Problem(
            type = tokenNotRevoked,
            title = "Token not revoked",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Token not revoked",
            instance = instance
        ).toResponse()

        fun userOrPasswordAreInvalid(instance: URI?): ResponseEntity<*> = Problem(
            type = userOrPasswordAreInvalid,
            title = "User or password are invalid",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "User or password are invalid",
            instance = instance
        ).toResponse()

        fun unauthorized(instance: URI?): ResponseEntity<*> = Problem(
            type = unauthorized,
            title = "Unauthorized",
            status = HttpStatus.UNAUTHORIZED.value(),
            detail = "The request has not been applied because it lacks valid authentication credentials for the target resource.",
            instance = instance
        ).toResponse()

        fun userNotInChannel(username: String, instance: URI?): ResponseEntity<*> = Problem(
            type = userNotInChannel,
            title = "User not in channel",
            status = HttpStatus.NOT_FOUND.value(),
            detail = "User $username not in channel",
            instance = instance
        ).toResponse()

        fun userIsNotChannelOwner(username: String, instance: URI?) : ResponseEntity<*> = Problem(
            type = userIsNotChannelOwner,
            title = "User is not channel owner",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "User $username is not channel owner",
            instance = instance
        ).toResponse()

        fun channelNameAlreadyExists(name: String, instance: URI?): ResponseEntity<*> = Problem(
            type = channelNameAlreadyExists,
            title = "Channel name already exists",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Channel name $name already exists",
            instance = instance
        ).toResponse()

        fun badRequest(instance: URI?): ResponseEntity<*> = Problem(
            type = badRequest,
            title = "Bad request",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "The request could not be understood by the server due to malformed syntax",
            instance = instance
        ).toResponse()

        fun userAlreadyInChannel(username: String, instance: URI?): ResponseEntity<*> = Problem(
            type = userAlreadyInChannel,
            title = "User already in channel",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "User $username already in channel",
            instance = instance
        ).toResponse()

        fun invalidInviteRegister(instance: URI?): ResponseEntity<*> = Problem(
            type = invalidInviteCode,
            title = "Invitation code is invalid",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Invitation code is invalid, please provide a valid invitation code",
            instance = instance
        ).toResponse()

        fun userPermissionsDeniedType(username: String, instance: URI?): ResponseEntity<*> = Problem(
            type = userPermissionsDeniedType,
            title = "User not permissions type",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "User $username not permissions type",
            instance = instance
        ).toResponse()

        fun codeInvalidOrExpiredChannel(code: String, instance: URI?): ResponseEntity<*> = Problem(
            type = codeInvalidOrExpiredChannel,
            title = "Code invalid channel",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Code $code is invalid or expired",
            instance = instance
        ).toResponse()

        fun channelIsPrivate(channelId: Int, instance: URI?): ResponseEntity<*> = Problem(
            type = channelIsPrivate,
            title = "Is private channel",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Channel $channelId is private",
            instance = instance
        ).toResponse()

        fun channelIsPublic(channelId: Int, instance: URI?): ResponseEntity<*> = Problem(
            type = channelIsPublic,
            title = "Channel is public",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Channel $channelId is public",
            instance = instance
        ).toResponse()

        fun privacyTypeInvalid(privacy: String, instance: URI?): ResponseEntity<*> = Problem(
            type = privacyTypeInvalid,
            title = "Privacy type invalid",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Privacy type $privacy invalid",
            instance = instance
        ).toResponse()

        fun userPrivacyTypeReadOnly(username: String, instance: URI?): ResponseEntity<*> = Problem(
            type = userPrivacyTypeReadOnly,
            title = "User privacy type read only",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "User $username privacy type read only",
            instance = instance
        ).toResponse()

        fun errorLeavingChannel(channelId: Int, instance: URI?): ResponseEntity<*> = Problem(
            type = errorLeavingChannel,
            title = "Error leaving channel",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "Error leaving channel $channelId",
            instance = instance
        ).toResponse()

        fun messageNotFound(messageId: Int, instance: URI?): ResponseEntity<*> = Problem(
            type = messageNotFound,
            title = "Message not found",
            status = HttpStatus.NOT_FOUND.value(),
            detail = "Message with id $messageId not found",
            instance = instance
        ).toResponse()
    }
}
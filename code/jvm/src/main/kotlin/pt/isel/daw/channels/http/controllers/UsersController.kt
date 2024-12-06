package pt.isel.daw.channels.http.controllers

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import kotlinx.datetime.Clock
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import pt.isel.daw.channels.domain.user.AuthenticatedUser
import pt.isel.daw.channels.http.Uris
import pt.isel.daw.channels.http.media.Problem
import pt.isel.daw.channels.http.model.user.*
import pt.isel.daw.channels.http.model.utils.IdOutputModel
import pt.isel.daw.channels.services.user.TokenCreationError
import pt.isel.daw.channels.services.user.UserCreationError
import pt.isel.daw.channels.services.user.UserSearchError
import pt.isel.daw.channels.services.user.UsersService
import pt.isel.daw.channels.utils.Failure
import pt.isel.daw.channels.utils.Success

@RestController
class UsersController(
    private val userService: UsersService
) {

    companion object {
        const val HEADER_SET_COOKIE_NAME = "Set-Cookie"
        const val COOKIE_NAME_LOGIN = "login"
        const val COOKIE_NAME_TOKEN = "token"
    }

    @PostMapping(Uris.Users.CREATE)
    fun create(
        @Validated @RequestBody input: UserCreateInputModel
    ): ResponseEntity<*> {
        val instance = Uris.Users.register()
        val user = userService.createUser(input.username, input.email, input.password, input.inviteCode)
        return when (user) {
            is Success -> ResponseEntity.status(201)
                .header(
                    "Location",
                    Uris.Users.byId(user.value).toASCIIString()
                ).body(IdOutputModel(user.value))

            is Failure -> when (user.value) {
                UserCreationError.InsecurePassword -> Problem.insecurePassword(instance)
                UserCreationError.UserNameAlreadyExists -> Problem.usernameAlreadyExists(input.username, instance)
                UserCreationError.EmailAlreadyExists -> Problem.emailAlreadyExists(input.email, instance)
                UserCreationError.InvalidInviteCode -> Problem.invalidInviteRegister(instance)
            }
        }
    }

    /**
     * Token creation
     * @param input UserCreateTokenInputModel
     * @param response HttpServletResponse
     * @return ResponseEntity<*>
     *
     * HttpOnly: The HttpOnly attribute is used to help prevent attacks such as cross-site scripting, since it does not allow the cookie to be accessed via JavaScript.
     * SameSite: The SameSite attribute is used to prevent the browser from sending this cookie along with cross-site requests. The main goal is mitigate the risk of cross-origin information leakage.
     * Path: The Path attribute indicates a URL path that must exist in the requested URL in order to send the Cookie header.
     * Max-age: The Max-age attribute is used to set the time in seconds for a cookie to expire.
     */

    @PostMapping(Uris.Users.TOKEN)
    fun token(
        @RequestBody input: UserCreateTokenInputModel,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        val instance = Uris.Users.login()
        val token = userService.createToken(input.username, input.password)
        return when (token) {
            is Success -> {
                // Cookie max age is the difference between the token expiration and the current time
                val cookieMaxAge = token.value.tokenExpiration.epochSeconds - Clock.System.now().epochSeconds
                ResponseEntity.status(200)
                    .header(
                        HEADER_SET_COOKIE_NAME,
                        "$COOKIE_NAME_TOKEN=${token.value.tokenValue};Max-age=$cookieMaxAge; HttpOnly; SameSite = Strict; Path=/"
                    )
                    .header(
                        HEADER_SET_COOKIE_NAME,
                        "$COOKIE_NAME_LOGIN=${input.username};Max-age=$cookieMaxAge; SameSite = Strict; Path=/"
                    )
                    .body(UserTokenCreateOutputModel(token.value.tokenValue))
            }

            is Failure -> when (token.value) {
                TokenCreationError.UserOrPasswordAreInvalid -> Problem.userOrPasswordAreInvalid(instance)
                TokenCreationError.InvalidToken -> Problem.invalidToken(instance)
            }
        }
    }


    @PostMapping(Uris.Users.LOGOUT)
    fun logout(
        auth: AuthenticatedUser,
    ): ResponseEntity<*> {
        val instance = Uris.Users.logout()
        return when (userService.revokeToken(auth.user.id, auth.token)) {
            is Success -> ResponseEntity.status(200)
                .header(
                    HEADER_SET_COOKIE_NAME,
                    "$COOKIE_NAME_TOKEN=${auth.token};Max-age=0; HttpOnly; SameSite = Strict; Path=/"
                )
                .header(
                    HEADER_SET_COOKIE_NAME,
                    "$COOKIE_NAME_LOGIN=${auth.user.username};Max-age=0; SameSite = Strict; Path=/"
                )
                .body(UserTokenCreateOutputModel("Token ${auth.token} removed successful"))

            is Failure -> Problem.tokenNotRevoked(instance, auth.token)
        }
    }

    @GetMapping(Uris.Users.GET_BY_ID)
    fun getById(
        @PathVariable id: Int,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Users.byId(id)
        val user = userService.getUserById(id)
        return when (user) {
            is Success ->
                ResponseEntity.status(200)
                    .body(UserHomeOutputModel(user.value.id, user.value.username))

            is Failure -> when (user.value) {
                UserSearchError.UserNotFound -> Problem.userNotFound(id, instance)
            }

        }
    }

    @GetMapping(Uris.Users.SEARCH_USERS)
    fun searchUsers(
        @RequestParam username: String,
        authUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val user = userService.searchUsers(username)
        return ResponseEntity.status(200)
            .body(UserListOutputModel(user.map { UserHomeOutputModel(it.id, it.username) }))
    }

    @PostMapping(Uris.Users.INVITE)
    fun createInvitationRegister(
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val userId = authenticatedUser.user.id
        val res = userService.createRegisterInvite(userId)
        return ResponseEntity
            .status(201)
            .body(UserInviteOutputModel(res))
    }

    @GetMapping(Uris.Users.HOME)
    fun getUserHome(userAuthenticatedUser: AuthenticatedUser): UserHomeOutputModel {
        return UserHomeOutputModel(
            id = userAuthenticatedUser.user.id,
            username = userAuthenticatedUser.user.username,
        )
    }

}
package pt.isel.daw.channels.http.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.daw.channels.domain.user.AuthenticatedUser
import pt.isel.daw.channels.http.Uris
import pt.isel.daw.channels.http.model.Problem
import pt.isel.daw.channels.http.model.user.UserCreateInputModel
import pt.isel.daw.channels.http.model.user.UserCreateTokenInputModel
import pt.isel.daw.channels.http.model.user.UserHomeOutputModel
import pt.isel.daw.channels.http.model.user.UserTokenCreateOutputModel
import pt.isel.daw.channels.services.user.TokenCreationError
import pt.isel.daw.channels.services.user.UserCreationError
import pt.isel.daw.channels.services.user.UserSearchError
import pt.isel.daw.channels.services.user.UsersService
import pt.isel.daw.channels.utils.Failure
import pt.isel.daw.channels.utils.Success

@RestController
class UsersController(private val userService: UsersService) {
    @PostMapping(Uris.Users.CREATE)
    fun create(
        @RequestBody input: UserCreateInputModel
    ): ResponseEntity<*> {
        val instance = Uris.Users.register()
        val user = userService.createUser(input.username, input.email, input.password)
        return when (user) {
            is Success -> ResponseEntity.status(201)
                .header(
                    "Location",
                    Uris.Users.byId(user.value).toASCIIString()
                ).build<Unit>()

            is Failure -> when (user.value) {
                UserCreationError.InsecurePassword -> Problem.insecurePassword(instance)
                UserCreationError.UserNameAlreadyExists -> Problem.usernameAlreadyExists(input.username, instance)
                UserCreationError.EmailAlreadyExists -> Problem.emailAlreadyExists(input.email, instance)
            }
        }
    }

    @PostMapping(Uris.Users.TOKEN)
    fun token(
        @RequestBody input: UserCreateTokenInputModel,
    ): ResponseEntity<*> {
        val instance = Uris.Users.login()
        val token = userService.createToken(input.username, input.password)
        return when (token) {
            is Success ->
                ResponseEntity.status(200)
                    .body(UserTokenCreateOutputModel(token.value.tokenValue))

            is Failure -> when (token.value) {
                TokenCreationError.UserOrPasswordAreInvalid -> Problem.userOrPasswordAreInvalid(instance)
                TokenCreationError.InvalidToken -> Problem.invalidToken(instance)
            }
        }
    }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(
        user: AuthenticatedUser,
    ) {
        userService.revokeToken(user.token)
    }

    @GetMapping(Uris.Users.GET_BY_ID)
    fun getById(
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val instance = Uris.Users.home()
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


    @GetMapping(Uris.Users.HOME)
    fun getUserHome(userAuthenticatedUser: AuthenticatedUser): UserHomeOutputModel {
        return UserHomeOutputModel(
            id = userAuthenticatedUser.user.id,
            username = userAuthenticatedUser.user.username,
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(UsersController::class.java)
    }

}
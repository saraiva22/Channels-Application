package pt.isel.daw.channels.http.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.channels.http.Uris
import pt.isel.daw.channels.http.model.Problem
import pt.isel.daw.channels.http.model.user.UserCreateInputModel
import pt.isel.daw.channels.services.user.UserCreationError
import pt.isel.daw.channels.services.user.UsersService
import pt.isel.daw.channels.utils.Failure
import pt.isel.daw.channels.utils.Success

@RestController
class UsersController(private val userService: UsersService) {
    @PostMapping(Uris.Users.CREATE)
    fun create(
        @RequestBody input: UserCreateInputModel
    ): ResponseEntity<*> {
        val user = userService.createUser(input.username, input.email, input.password)
        return when (user) {
            is Success -> ResponseEntity.status(201)
                .header(
                    "Location",
                    Uris.Users.byId(user.value).toASCIIString()
                ).build<Unit>()

            is Failure -> when (user.value) {
                UserCreationError.InsecurePassword -> Problem.response(400, Problem.insecurePassword)
                UserCreationError.UserNameAlreadyExists -> Problem.response(400, Problem.userAlreadyExists)
                UserCreationError.EmailAlreadyExists -> Problem.response(400, Problem.emailAlreadyExists)
            }
        }
    }

    companion object{
        private val log = LoggerFactory.getLogger(UsersController::class.java)
    }

}
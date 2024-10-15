package pt.isel.daw.channels.http.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.channels.domain.user.AuthenticatedUser
import pt.isel.daw.channels.http.Uris
import pt.isel.daw.channels.http.model.Problem
import pt.isel.daw.channels.http.model.message.MessageListOutputModel
import pt.isel.daw.channels.services.message.GetMessageError
import pt.isel.daw.channels.services.message.MessagesService
import pt.isel.daw.channels.utils.Failure
import pt.isel.daw.channels.utils.Success

@RestController
class MessagesController (
    private val messagesService: MessagesService
) {

    @PostMapping(Uris.Messages.CREATE)
    fun createMessage() {
        TODO()
    }

    @GetMapping(Uris.Messages.GET_BY_CHANNEL)
    fun getMessagesByChannel(
        @PathVariable id : Int,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Messages.byChannel(id)
        val userId = authenticatedUser.user.id
        return when (val res = messagesService.getChannelMessages(userId, id)) {
            is Success -> ResponseEntity
                .status(200)
                .body(MessageListOutputModel(res.value))

            is Failure -> when(res.value) {
                GetMessageError.ChannelNotFound -> Problem.channelNotFound(id, instance)
                GetMessageError.PermissionDenied -> Problem.unauthorized(instance)
            }
        }
    }
}
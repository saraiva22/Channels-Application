package pt.isel.daw.channels.http.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.channels.domain.user.AuthenticatedUser
import pt.isel.daw.channels.http.Uris
import pt.isel.daw.channels.http.media.Problem
import pt.isel.daw.channels.http.model.message.MessageCreateInputModel
import pt.isel.daw.channels.http.model.message.MessageListOutputModel
import pt.isel.daw.channels.services.message.CreateMessageError
import pt.isel.daw.channels.services.message.GetMessageError
import pt.isel.daw.channels.services.message.MessagesService
import pt.isel.daw.channels.utils.Failure
import pt.isel.daw.channels.utils.Success

@RestController
class MessagesController(
    private val messagesService: MessagesService
) {

    @PostMapping(Uris.Messages.CREATE)
    fun createMessage(
        @PathVariable id: Int,
        @RequestBody input: MessageCreateInputModel,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Messages.create(id)
        val message = messagesService.createMessage(id, authenticatedUser.user, input.text)
        return when (message) {
            is Success -> ResponseEntity.status(201)
                .header(
                    "Location",
                    Uris.Messages.byId(id, message.value).toASCIIString()
                ).build<Unit>()

            is Failure -> when (message.value) {
                CreateMessageError.ChannelNotFound -> Problem.channelNotFound(id, instance)
                CreateMessageError.UserNotMemberInChannel -> Problem.userNotInChannel(
                    authenticatedUser.user.username,
                    instance
                )

                CreateMessageError.PrivacyIsNotReadWrite -> Problem.userPrivacyTypeReadOnly(
                    authenticatedUser.user.username,
                    instance
                )

            }
        }
    }

    @GetMapping(Uris.Messages.GET_BY_CHANNEL)
    fun getMessagesByChannel(
        @PathVariable id: Int,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Messages.byChannel(id)
        val userId = authenticatedUser.user.id
        val res = messagesService.getChannelMessages(userId, id)
        logger.info(res.toString())
        return when (res) {
            is Success -> ResponseEntity
                .status(200)
                .body(MessageListOutputModel(res.value))

            is Failure -> when (res.value) {
                GetMessageError.ChannelNotFound -> Problem.channelNotFound(id, instance)
                GetMessageError.PermissionDenied -> Problem.unauthorized(instance)
            }
        }
    }

    @DeleteMapping(Uris.Messages.DELETE)
    fun deleteMessagesByChannel() {
        TODO()
    }

    companion object {
        val logger = LoggerFactory.getLogger(MessagesController::class.java)
    }
}
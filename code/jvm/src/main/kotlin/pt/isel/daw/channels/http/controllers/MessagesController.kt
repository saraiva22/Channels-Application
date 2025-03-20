package pt.isel.daw.channels.http.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.daw.channels.domain.user.AuthenticatedUser
import pt.isel.daw.channels.http.Uris
import pt.isel.daw.channels.http.media.Problem
import pt.isel.daw.channels.http.model.message.MessageCreateInputModel
import pt.isel.daw.channels.http.model.message.MessageListOutputModel
import pt.isel.daw.channels.http.model.message.MessageOutputModel
import pt.isel.daw.channels.http.model.utils.IdOutputModel
import pt.isel.daw.channels.services.message.*
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
        return when (val message = messagesService.createMessage(id, authenticatedUser.user, input.text)) {
            is Success -> ResponseEntity.status(201)
                .header(
                    "Location",
                    Uris.Messages.byId(id, message.value).toASCIIString()
                ).body(IdOutputModel(message.value))

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

    @GetMapping(Uris.Messages.GET_MESSAGES)
    fun getMessagesByChannel(
        @PathVariable id: Int,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Messages.byChannel(id)
        val userId = authenticatedUser.user.id
        return when (val res = messagesService.getChannelMessages(userId, id)) {
            is Success -> {
                val outputRes = res.value.map {
                    MessageOutputModel(
                        it.id,
                        it.text,
                        it.channel,
                        it.user,
                        it.created.toString()
                    )
                }
                ResponseEntity
                    .status(200)
                    .body(MessageListOutputModel(outputRes))
            }

            is Failure -> when (res.value) {
                GetMessagesError.ChannelNotFound -> Problem.channelNotFound(id, instance)
                GetMessagesError.PermissionDenied -> Problem.userPermissionsDenied(instance)
            }
        }
    }

    @GetMapping(Uris.Messages.GET_BY_ID)
    fun getMessagesById(
        @PathVariable channelId: Int,
        @PathVariable messageId: Int,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Messages.byId(channelId, messageId)
        val userId = authenticatedUser.user.id
        return when (val res = messagesService.getMessageById(userId, messageId, channelId)) {
            is Success ->
                ResponseEntity
                    .status(200)
                    .body(
                        MessageOutputModel(
                            res.value.id,
                            res.value.text,
                            res.value.channel,
                            res.value.user,
                            res.value.created.toString()
                        )
                    )

            is Failure -> when (res.value) {
                GetMessageError.ChannelNotFound -> Problem.channelNotFound(channelId, instance)
                GetMessageError.PermissionDenied -> Problem.userPermissionsDenied(instance)
                GetMessageError.MessageNotFound -> Problem.messageNotFound(messageId, instance)
            }
        }
    }


    @DeleteMapping(Uris.Messages.DELETE)
    fun deleteMessagesByChannel(
        @PathVariable channelId: Int,
        @PathVariable id: Int,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Messages.delete(channelId, id)
        val userId = authenticatedUser.user.id
        return when (val res = messagesService.deleteMessageFromChannel(userId, id, channelId)) {
            is Success -> ResponseEntity
                .status(200)
                .build<Unit>()

            is Failure -> when (res.value) {
                DeleteMessageError.ChannelNotFound -> Problem.channelNotFound(id, instance)
                DeleteMessageError.PermissionDenied -> Problem.userPermissionsDenied(instance)
                DeleteMessageError.MessageNotFound -> Problem.messageNotFound(id, instance)
            }
        }
    }

}
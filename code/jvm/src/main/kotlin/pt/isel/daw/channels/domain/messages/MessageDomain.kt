package pt.isel.daw.channels.domain.messages

import org.springframework.stereotype.Component

/**
 * Represents a Message Domain
 */
@Component
class MessageDomain {

    fun isMessageInList(messageId: Int, list: List<Message>) =
        list.filter { message -> message.id == messageId }.size == 1

    fun isCreatorOfMessage(userId: Int, message: Message) = userId == message.user.id
}
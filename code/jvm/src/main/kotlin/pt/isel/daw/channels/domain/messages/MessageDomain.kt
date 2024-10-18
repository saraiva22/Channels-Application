package pt.isel.daw.channels.domain.messages

import org.springframework.stereotype.Component

@Component
class MessageDomain {

    fun isMessageInList(messageId: Int, list: List<Message>) =
        list.filter { message -> message.id == messageId }.size == 1
}
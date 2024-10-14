package pt.isel.daw.channels.repository

import pt.isel.daw.channels.domain.messages.Message
import pt.isel.daw.channels.domain.messages.MessageModel

interface MessagesRepository {

    fun createMessage(message: MessageModel): Int

    fun getChannelMessages(channelId: Int): List<Message>

    fun deleteMessageFromChannel(messageId: Int, channelId: Int): Boolean
}
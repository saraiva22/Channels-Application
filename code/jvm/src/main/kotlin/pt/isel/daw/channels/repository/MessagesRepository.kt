package pt.isel.daw.channels.repository

import kotlinx.datetime.Instant
import pt.isel.daw.channels.domain.messages.Message
import pt.isel.daw.channels.domain.messages.MessageModel

interface MessagesRepository {

    fun createMessage(channelId: Int,userId:Int, text: String,createAt: Instant): Int

    fun getChannelMessages(channelId: Int): List<Message>

    fun deleteMessageFromChannel(messageId: Int, channelId: Int): Boolean
}
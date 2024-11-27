package pt.isel.daw.channels.repository

import kotlinx.datetime.Instant
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.messages.Message

interface MessagesRepository {

    fun createMessage(channelId: Int, userId: Int, text: String, createAt: Instant): Int

    fun getChannelMessages(channel: Channel): List<Message>

    fun deleteMessageFromChannel(messageId: Int, channelId: Int): Int

    fun getMessageById(messageId: Int, channel: Channel): Message?

}
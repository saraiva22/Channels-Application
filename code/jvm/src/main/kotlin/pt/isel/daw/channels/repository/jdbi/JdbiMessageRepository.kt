package pt.isel.daw.channels.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.channels.domain.messages.Message
import pt.isel.daw.channels.domain.messages.MessageModel
import pt.isel.daw.channels.repository.MessagesRepository

class JdbiMessageRepository(
    private val handle: Handle
): MessagesRepository {
    override fun createMessage(message: MessageModel): Int {
        TODO("Not yet implemented")
    }

    override fun getChannelMessages(channelId: Int): List<Message> =
        handle.createQuery(
            """
                    select messages.id, messages.text, messages.channel_id as channel,
                    messages.user_id as user, messages.create_at as creation
                    from dbo.Messages as messages
                    join dbo.Channels as channels on messages.channel_id = channels.id
                    where channels.id = :id
                """
        )
            .bind("id", channelId)
            .mapTo<Message>()
            .list()

    override fun deleteMessageFromChannel(messageId: Int, channelId: Int): Boolean {
        TODO("Not yet implemented")
    }
}
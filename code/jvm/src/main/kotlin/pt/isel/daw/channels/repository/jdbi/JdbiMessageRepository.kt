package pt.isel.daw.channels.repository.jdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.channels.domain.messages.Message
import pt.isel.daw.channels.domain.messages.MessageModel
import pt.isel.daw.channels.repository.MessagesRepository

class JdbiMessageRepository(
    private val handle: Handle
) : MessagesRepository {
    override fun createMessage(channelId: Int, userId: Int, text: String, createAt: Instant): Int {
        return handle.createUpdate(
            """
                insert into dbo.Messages(channel_id,user_id, text, create_at) 
                values(:channelId,:userId,:text,:createAt)
            """
        ).bind("channelId", channelId)
            .bind("userId", userId)
            .bind("text", text)
            .bind("createAt", createAt.epochSeconds)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
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
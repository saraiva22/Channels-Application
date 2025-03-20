package pt.isel.daw.channels.repository.jdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.messages.Message
import pt.isel.daw.channels.http.model.message.MessageDbModel
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
        )
            .bind("channelId", channelId)
            .bind("userId", userId)
            .bind("text", text)
            .bind("createAt", createAt.epochSeconds)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun getChannelMessages(channel: Channel): List<Message> {
        val messageDbModelList = handle.createQuery(
            """ 
                select messages.id, messages.text,
                users.id as user_id, users.email as user_email, users.username as user_username,
                messages.create_at as created
                from dbo.Messages as messages
                join dbo.Channels as channels on messages.channel_id = channels.id
                join dbo.Users as users on messages.user_id = users.id
                where messages.channel_id = :channelId
                order by messages.create_at
            """
        )
            .bind("channelId", channel.id)
            .mapTo<MessageDbModel>()
            .list()

        return messageDbModelList.map {
            it.toMessage(channel)
        }
    }

    override fun deleteMessageFromChannel(messageId: Int, channelId: Int): Int =
        handle.createUpdate(
            """
                delete from dbo.Messages
                where id = :messageId and channel_id = :channelId
            """
        )
            .bind("messageId", messageId)
            .bind("channelId", channelId)
            .execute()


    override fun getMessageById(messageId: Int, channel: Channel): Message? {
        val message = handle.createQuery(
            """ 
            select messages.id, messages.text,
            users.id as user_id, users.email as user_email, users.username as user_username,
            messages.create_at as created
            from dbo.Messages as messages
            join dbo.Users as users on messages.user_id = users.id
            where messages.id = :messageId and messages.channel_id = :channelId
            """
        )
            .bind("messageId", messageId)
            .bind("channelId", channel.id)
            .mapTo<MessageDbModel>()
            .firstOrNull()

        return message?.toMessage(channel)
    }


}
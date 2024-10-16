package pt.isel.daw.channels.repository.jdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
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

    override fun getChannelMessages(channelId: Int): List<Message> =
        handle.createQuery(
            """
                select messages.id as id, messages.text as text, 
                channels.id as channel_id, channels.name as channel_name, channels.owner_id as channel_owner, 
                coalesce(array_agg(members_table.user_id) filter (where members_table.user_id is not null), '{}') as channel_members,
                users.id as user_id, users.email as user_email, users.username as user_username, users.password_validation as user_passwordValidation,
                messages.create_at as created
                from dbo.Messages as messages
                join dbo.Channels as channels on messages.channel_id = channels.id
                join dbo.Join_Channels as members_table on messages.channel_id = members_table.ch_id
                join dbo.Users as users on messages.user_id = users.id
                where channels.id = :id
                group by messages.id, channels.id, users.id
            """
        )
            .bind("id", channelId)
            .mapTo<MessageDbModel>()
            .list().run {
                this.map { it.toMessage() }
            }

    override fun deleteMessageFromChannel(messageId: Int, channelId: Int): Boolean {
        TODO("Not yet implemented")
    }

}
package pt.isel.daw.channels.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.repository.ChannelsRepository

class JdbiChannelsRepository(
    private val handle: Handle
): ChannelsRepository {
    override fun createChannel(channel: ChannelModel): Int {
        val channelId = handle.createUpdate(
            """
                insert into dbo.Channels(name, owner_id, rules) values (:name, :owner_id, :rules)
            """
        )
            .bind("name", channel.name)
            .bind("owner_id", channel.owner)
            .bind("rules", channel.rules)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

        val insertChannel = if (channel.type == Type.PUBLIC) {
            """
            insert into dbo.Public_Channels(channel_id) values (:channel_id)
            """
        } else {
            """
            insert into dbo.Private_Channels(channel_id) values (:channel_id)
            """
        }

        handle.createUpdate(insertChannel)
            .bind("channel_id", channelId)
            .execute()

        return channelId
    }

    override fun isChannelStored(channelName: String): Boolean =
        handle.createQuery("select count(*) from dbo.Channels where name = :name")
            .bind("name", channelName)
            .mapTo<Int>()
            .single() == 1

    override fun joinChannel(userId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun getChannels(userId: Int): List<Channel> {
        TODO("Not yet implemented")
    }

    override fun getPublicChannels(): List<Channel> {
        TODO("Not yet implemented")
    }

    override fun leaveChannel(channel: Channel, userId: Int): Boolean {
        TODO("Not yet implemented")
    }
}
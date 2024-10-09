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
                insert into dbo.Channels(name, owner_id) values (:name, :owner_id)
            """
        )
            .bind("name", channel.name)
            .bind("owner_id", channel.owner)
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

    override fun isChannelStoredByName(channelName: String): Boolean =
        handle.createQuery("select count(*) from dbo.Channels where name = :name")
            .bind("name", channelName)
            .mapTo<Int>()
            .single() == 1

    override fun getChannelById(channelId: Int): Channel? =
        handle.createQuery(
            """
                select channels.id, channels.name, channels.owner_id as owner, 
                coalesce(array_agg(members_table.user_id) filter (where members_table.user_id is not null), '{}') as members
                from dbo.Channels as channels 
                left join dbo.Join_Channels as members_table on channels.id = members_table.ch_id 
                where channels.id = :id 
                group by channels.id, channels.name, channels.owner_id
            """
        )
            .bind("id", channelId)
            .mapTo<Channel>()
            .singleOrNull()

    override fun getChannelByName(channelName: String): Channel? =
        handle.createQuery(
            """
                select channels.id, channels.name, channels.owner_id as owner, 
                coalesce(array_agg(members_table.user_id) filter (where members_table.user_id is not null), '{}') as members
                from dbo.Channels as channels 
                left join dbo.Join_Channels as members_table on channels.id = members_table.ch_id 
                where channels.name = :name 
                group by channels.id, channels.name, channels.owner_id
            """
        )
            .bind("name", channelName)
            .mapTo<Channel>()
            .singleOrNull()

    override fun joinChannel(userId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun getChannels(userId: Int): List<Channel> {
        TODO("Not yet implemented")
    }

    override fun getPublicChannels(): List<Channel> =
        handle.createQuery(
            """
                select channels.id, channels.name, channels.owner_id as owner,
                coalesce(array_agg(members_table.user_id) filter (where members_table.user_id is not null), '{}') as members
                from dbo.Channels as channels
                left join dbo.Join_Channels as members_table on channels.id = members_table.ch_id
                join dbo.Public_Channels as public on channels.id = public.channel_id
                group by channels.id, channels.name, channels.owner_id
            """
        )
            .mapTo<Channel>()
            .list()

    override fun isChannelPublic(channel: Channel): Boolean {
        val publicChannels = getPublicChannels()
        return publicChannels.contains(channel)
    }

    override fun leaveChannel(channel: Channel, userId: Int): Boolean {
        TODO("Not yet implemented")
    }
}
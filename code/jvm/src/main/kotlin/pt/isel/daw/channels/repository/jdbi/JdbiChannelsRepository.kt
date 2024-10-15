package pt.isel.daw.channels.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.repository.ChannelsRepository

class JdbiChannelsRepository(
    private val handle: Handle
) : ChannelsRepository {
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

    override fun getUserChannels(userId: Int): List<Channel> =
        handle.createQuery(
            """
                select channels.id, channels.name, channels.owner_id as owner, 
                coalesce(array_agg(members_table.user_id) filter (where members_table.user_id is not null), '{}') as members 
                from dbo.Channels as channels
                join dbo.Join_Channels as user_channels on channels.id = user_channels.ch_id
                left join dbo.Join_Channels as members_table on channels.id = members_table.ch_id
                where user_channels.user_id = :userId
                group by channels.id, channels.name, channels.owner_id
            """
        )
            .bind("userId", userId)
            .mapTo<Channel>()
            .list()

    override fun getUserChannel(channelId: Int, userId: Int): Channel? =
        handle.createQuery(
            """
                select channels.id, channels.name, channels.owner_id as owner, 
                coalesce(array_agg(members_table.user_id) filter (where members_table.user_id is not null), '{}') as members
                from dbo.Join_Channels as members_table
                join dbo.Channels as channels on members_table.ch_id = channels.id
                where members_table.ch_id = :channelId and members_table.user_id = :userId
                group by channels.id, channels.name, channels.owner_id;
            """
        )
            .bind("channelId", channelId)
            .bind("userId", userId)
            .mapTo<Channel>()
            .singleOrNull()


    override fun joinChannel(userId: Int, channelId: Int): Channel {
        handle.createUpdate(
            """
        insert into dbo.join_channels (user_id, ch_id) values (:userId, :channelId)
        """
        )
            .bind("userId", userId)
            .bind("channelId", channelId)
            .execute()

        return handle.createQuery(
            """
        select channels.id, channels.name, channels.owner_id as owner,
        coalesce(array_agg(members_table.user_id) filter (where members_table.user_id is not null), '{}') as members
        from dbo.Channels as channels
        left join dbo.Join_Channels as members_table on channels.id = members_table.ch_id
        where channels.id = :channelId
        group by channels.id, channels.name, channels.owner_id
        """
        )
            .bind("channelId", channelId)
            .mapTo<Channel>()
            .one()
    }

    override fun isOwnerChannel(channelId: Int, userId: Int): Boolean {
        val channel = getChannelById(channelId)
        return channel?.owner == userId
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

    override fun updateChannelName(channelId: Int, name: String): Channel {
        handle.createUpdate(
            """
        update dbo.Channels set name = :name where id = :id
        """
        )
            .bind("name", name)
            .bind("id", channelId)
            .execute()

        return handle.createQuery(
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
            .one()
    }

    override fun createPrivateInvite(codPrivate: String): Int {
        return handle.createUpdate(
            """
        insert into dbo.Invitation_Channels(cod_hash) values (:codPrivate)
            """
        ).bind("codPrivate", codPrivate)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun sendInvitePrivateChannel(userId: Int, channelId: Int, inviteId: Int, privacy: Int): Int {
        return handle.createUpdate(
            """
        insert into dbo.Invite_Private_Channels(user_id, private_ch, invite_id, privacy) values (:userId, :channelId, :inviteId, :privacy)
            """
        ).bind("userId", userId)
            .bind("channelId", channelId)
            .bind("inviteId", inviteId)
            .bind("privacy", privacy)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun getTypeInvitePrivateChannel(userId: Int, channelId: Int): Privacy? {
        val privacyValue = handle.createQuery(
            """
        select privacy from dbo.Invite_Private_Channels where user_id = :userId and private_ch = :channelId
        """
        ).bind("userId", userId)
            .bind("channelId", channelId)
            .mapTo<Int>()
            .one()

        return Privacy.fromInt(privacyValue)
    }

    override fun isPrivateChannelInviteCodeValid(userId: Int, channelId: Int, inviteId: String): Boolean {
        return handle.createQuery(
            """
        select count(*)
        from dbo.invite_private_channels as ipc
        join dbo.invitation_channels as ic on ipc.invite_id = ic.id
        where ipc.user_id = :userId and ipc.private_ch = :channelId and ic.cod_hash = :inviteId;
            """
        ).bind("userId", userId)
            .bind("channelId", channelId)
            .bind("inviteId", inviteId)
            .mapTo<Int>()
            .one() == 1
    }

}
package pt.isel.daw.channels.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.channels.Sort
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.domain.user.UserInfo
import pt.isel.daw.channels.http.model.channel.ChannelDbModel
import pt.isel.daw.channels.repository.ChannelsRepository

class JdbiChannelsRepository(
    private val handle: Handle
) : ChannelsRepository {
    override fun createChannel(channel: ChannelModel): Int {
        val typeId = channel.type.ordinal
        return handle.createUpdate(
            """
                insert into dbo.Channels(name, owner_id, type) values (:name, :owner_id, :type)
            """
        )
            .bind("name", channel.name)
            .bind("owner_id", channel.owner)
            .bind("type", typeId)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun isChannelStoredByName(channelName: String): Boolean =
        handle.createQuery("select count(*) from dbo.Channels where name = :name")
            .bind("name", channelName)
            .mapTo<Int>()
            .single() == 1

    override fun getChannelById(channelId: Int): Channel? {
        val channelDbModel = handle.createQuery(
            """
                select channels.id, channels.name, channels.type,
                users.id as owner_id, users.email as owner_email, 
                users.username as owner_username
                from dbo.Channels as channels 
                left join dbo.Users as users on channels.owner_id = users.id
                where channels.id = :id
            """
        )
            .bind("id", channelId)
            .mapTo<ChannelDbModel>()
            .singleOrNull()

        if (channelDbModel == null) return channelDbModel

        val members = getChannelMembers(channelId)

        return channelDbModel.copy(members = members).toChannel()
    }

    override fun searchChannelsByName(channelName: String, sort: Sort?): List<Channel> {
        val queryString =
            """
                select channels.id, channels.name, channels.type,
                users.id as owner_id, users.email as owner_email, 
                users.username as owner_username, users.password_validation as owner_passwordValidation
                from dbo.Channels as channels 
                left join dbo.Users as users on channels.owner_id = users.id
                where lower(channels.name) like lower(:name)
            """

        val withSortQuery = builtSortQuery(sort, queryString)

        val searchChannel = "%$channelName%"

        val channelDbModelList =
            handle.createQuery(withSortQuery)
                .bind("name", searchChannel)
                .mapTo<ChannelDbModel>()
                .list()

        return channelDbModelList.map { channel ->
            val members = getChannelMembers(channel.id)
            channel.copy(members = members).toChannel()
        }
    }

    override fun getUserOwnedChannels(userId: Int, sort: Sort?): List<Channel> {
        val queryString =
            """
                select channels.id, channels.name, channels.type,
                users.id as owner_id, users.email as owner_email, 
                users.username as owner_username, users.password_validation as owner_passwordValidation
                from dbo.Channels as channels 
                left join dbo.Users as users on channels.owner_id = users.id
                where users.id = :userId
            """

        val withSortQuery = builtSortQuery(sort, queryString)

        val channelDbModelList =
            handle.createQuery(withSortQuery)
                .bind("userId", userId)
                .mapTo<ChannelDbModel>()
                .list()

        return channelDbModelList.map { channel ->
            val members = getChannelMembers(channel.id)
            channel.copy(members = members).toChannel()
        }
    }

    override fun getAllChannels(sort: Sort?): List<Channel> {
        val queryString =
            """
                select channels.id, channels.name, channels.type,
                users.id as owner_id, users.email as owner_email, 
                users.username as owner_username, users.password_validation as owner_passwordValidation
                from dbo.Channels as channels 
                left join dbo.Users as users on channels.owner_id = users.id
            """

        val withSortQuery = builtSortQuery(sort, queryString)

        val channelDbModelList =
            handle.createQuery(withSortQuery)
                .mapTo<ChannelDbModel>()
                .list()

        return channelDbModelList.map { channel ->
            val members = getChannelMembers(channel.id)
            channel.copy(members = members).toChannel()
        }
    }

    private fun insertUserIntoChannel(userId: Int, channelId: Int) {
        handle.createUpdate(
            """
                insert into dbo.join_channels (user_id, ch_id) values (:userId, :channelId)
            """
        )
            .bind("userId", userId)
            .bind("channelId", channelId)
            .execute()
    }

    override fun joinChannel(userId: Int, channelId: Int): Channel {
        insertUserIntoChannel(userId, channelId)

        val channelDbModel = secureGetChannelById(channelId)
        val members = getChannelMembers(channelId)

        return channelDbModel.copy(members = members).toChannel()
    }

    override fun joinMemberInChannelPrivate(userId: Int, channelId: Int, codHas: String): Channel {
        insertUserIntoChannel(userId, channelId)

        val inviteId = handle.createQuery(
            """
                select invite_id
                from dbo.invite_private_channels as ipc
                join dbo.invitation_channels as ic on ipc.invite_id = ic.id
                where ipc.user_id = :userId and ipc.private_ch = :channelId and ic.cod_hash = :codHash
            """
        )
            .bind("userId", userId)
            .bind("channelId", channelId)
            .bind("codHash", codHas)
            .mapTo<Int>()
            .single()

        handle.createUpdate(
            """
            update dbo.invitation_channels set expired = :updateExpired where id = :inviteId
        """
        )
            .bind("inviteId", inviteId)
            .bind("updateExpired", true)
            .bind("userId", userId)
            .execute()

        val channelDbModel = secureGetChannelById(channelId)
        val members = getChannelMembers(channelId)

        return channelDbModel.copy(members = members).toChannel()
    }

    override fun isOwnerChannel(channelId: Int, userId: Int): Boolean {
        val channel = getChannelById(channelId)
        return channel?.owner?.id == userId
    }

    override fun getPublicChannels(sort: Sort?): List<Channel> {
        val queryString =
            """
                select channels.id, channels.name, channels.type,
                users.id as owner_id, users.email as owner_email, 
                users.username as owner_username, users.password_validation as owner_passwordValidation
                from dbo.Channels as channels 
                left join dbo.Users as users on channels.owner_id = users.id
                where channels.type = 0
            """

        val withSortQuery = builtSortQuery(sort, queryString)

        val channelDbModelList =
            handle.createQuery(withSortQuery)
                .mapTo<ChannelDbModel>()
                .list()

        return channelDbModelList.map { channel ->
            val members = getChannelMembers(channel.id)
            channel.copy(members = members).toChannel()
        }
    }

    override fun isChannelPublic(channel: Channel): Boolean {
        val publicChannels = getPublicChannels(null)
        return publicChannels.contains(channel)
    }

    override fun leaveChannel(userId: Int, channelId: Int): Boolean {
        return handle.createUpdate(
            """
                delete from dbo.Join_Channels where user_id = :userId and ch_id = :channelId
            """
        )
            .bind("userId", userId)
            .bind("channelId", channelId)
            .execute() == 1
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

        val channelDbModel = secureGetChannelById(channelId)

        val members = getChannelMembers(channelId)

        return channelDbModel.copy(members = members).toChannel()
    }

    override fun createPrivateInvite(codPrivate: String, expired: Boolean): Int {
        return handle.createUpdate(
            """
                insert into dbo.Invitation_Channels(cod_hash,expired) values (:codPrivate,:expired)
            """
        )
            .bind("codPrivate", codPrivate)
            .bind("expired", expired)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun sendInvitePrivateChannel(userId: Int, channelId: Int, inviteId: Int, privacy: Int): Int {
        return handle.createUpdate(
            """
                insert into dbo.Invite_Private_Channels(user_id, private_ch, invite_id, privacy) 
                values (:userId, :channelId, :inviteId, :privacy)
            """
        )
            .bind("userId", userId)
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
        )
            .bind("userId", userId)
            .bind("channelId", channelId)
            .mapTo<Int>()
            .one()

        return Privacy.fromInt(privacyValue)
    }

    override fun isPrivateChannelInviteCodeValid(
        userId: Int,
        channelId: Int,
        inviteId: String,
        expired: Boolean
    ): Channel? {
        val channelDbModel = handle.createQuery(
            """
                select c.id, c.name, c.type,
                u.id as owner_id, u.email as owner_email, 
                u.username as owner_username, u.password_validation as owner_passwordValidation
                from dbo.invite_private_channels as ipc
                join dbo.invitation_channels as ic on ipc.invite_id = ic.id
                join dbo.channels as c on c.id = ipc.private_ch
                join dbo.users as u on u.id = c.owner_id
                where ic.cod_hash = :inviteId and 
                ipc.user_id = :userId and 
                ipc.private_ch = :channelId and 
                ic.expired = :expired;
            """
        )
            .bind("inviteId", inviteId)
            .bind("userId", userId)
            .bind("channelId", channelId)
            .bind("expired", expired)
            .mapTo<ChannelDbModel>()
            .singleOrNull()

        if (channelDbModel == null) return channelDbModel

        val members = getChannelMembers(channelDbModel.id)

        return channelDbModel.copy(members = members).toChannel()
    }

    private fun getChannelMembers(channelId: Int) =
        handle.createQuery(
            """
                select users.id, users.email, users.username
                from dbo.Users as users
                join dbo.Join_Channels as members_table on members_table.user_id = users.id
                where members_table.ch_id = :channelId
            """
        )
            .bind("channelId", channelId)
            .mapTo<UserInfo>()
            .list()

    private fun secureGetChannelById(channelId: Int) =
        handle.createQuery(
            """
                select channels.id, channels.name, channels.type,
                users.id as owner_id, users.email as owner_email, 
                users.username as owner_username, users.password_validation as owner_passwordValidation
                from dbo.Channels as channels 
                left join dbo.Users as users on channels.owner_id = users.id
                where channels.id = :id
            """
        )
            .bind("id", channelId)
            .mapTo<ChannelDbModel>()
            .single()

    private fun builtSortQuery(sort: Sort?, queryString: String): String =
        if (sort != null && sort.value == "name") {
            "$queryString order by channels.name"
        } else {
            queryString
        }
}
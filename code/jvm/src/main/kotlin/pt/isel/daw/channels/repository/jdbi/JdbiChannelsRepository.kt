package pt.isel.daw.channels.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.channels.Sort
import pt.isel.daw.channels.domain.channels.Status
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

        val withSortQuery = buildSortQuery(sort, queryString)

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

        val withSortQuery = buildSortQuery(sort, queryString)

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

        val withSortQuery = buildSortQuery(sort, queryString)

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

    override fun joinMemberInChannelPrivate(userId: Int, channelId: Int, codHash: String): Channel {
        updateInviteStatus(Status.ACCEPT, codHash)

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

        val withSortQuery = buildSortQuery(sort, queryString)

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

    override fun createPrivateInvite(
        codPrivate: String,
        privacy: Int,
        inviterId: Int,
        guestId: Int,
        channelId: Int
    ): Int {
        return handle.createUpdate(
            """
                insert into dbo.Invitation_Channels(cod_hash, privacy, status, inviter_id, guest_id, private_ch) 
                values (:code, :privacy, :status, :inviterId, :guestId, :privateCh)
            """
        )
            .bind("code", codPrivate)
            .bind("privacy", privacy)
            .bind("status", Status.PENDING.ordinal)
            .bind("inviterId", inviterId)
            .bind("guestId", guestId)
            .bind("privateCh", channelId)
            .execute()
    }

    override fun getMemberPermissions(userId: Int, channelId: Int): Privacy {
        val privacyValue = handle.createQuery(
            """
                select privacy 
                from dbo.Invitation_Channels 
                where user_id = :userId and 
                      private_ch = :channelId and
                      status = :status
            """
        )
            .bind("userId", userId)
            .bind("channelId", channelId)
            .bind("status", Status.ACCEPT.ordinal)
            .mapTo<Int>()
            .one()

        return Privacy.fromDBInt(privacyValue)
    }

    override fun isInviteCodeValid(userId: Int, channelId: Int, codHash: String): Boolean {
        return handle.createQuery(
            """
                select count(*)
                from dbo.invitation_channels as ic 
                where ic.cod_hash = :codHash and 
                      ic.guest_id = :guestId and 
                      ic.private_ch = :channelId and
                      ic.status = :status;
            """
        )
            .bind("codHash", codHash)
            .bind("guestId", userId)
            .bind("channelId", channelId)
            .bind("status", Status.PENDING.ordinal)
            .mapTo<Int>()
            .single() == 1
    }

    override fun channelInviteRejected(userId: Int, channelId: Int, codHash: String) {
        updateInviteStatus(Status.REJECT, codHash)
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

    private fun buildSortQuery(sort: Sort?, queryString: String): String =
        if (sort != null && sort.value == "name") {
            "$queryString order by channels.name"
        } else {
            queryString
        }

    private fun updateInviteStatus(status: Status, codHash: String) =
        handle.createUpdate(
            """
               update dbo.Invitation_Channels set status = :status where cod_Hash = :codHash
            """
        )
            .bind("status", status.ordinal)
            .bind("codHash", codHash)
            .execute()
}
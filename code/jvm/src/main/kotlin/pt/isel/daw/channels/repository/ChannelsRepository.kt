package pt.isel.daw.channels.repository

import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.channels.Sort

interface ChannelsRepository {

    fun createChannel(channel: ChannelModel): Int

    fun isChannelStoredByName(channelName: String): Boolean

    fun isOwnerChannel(channelId: Int, userId: Int): Boolean

    fun getChannelById(channelId: Int): Channel?

    fun searchChannelsByName(channelName: String, sort: Sort?): List<Channel>

    fun getUserOwnedChannels(userId: Int, sort: Sort?): List<Channel>

    fun getAllChannels(sort: Sort?): List<Channel>

    fun joinChannel(userId: Int, channelId: Int): Channel

    fun joinMemberInChannelPrivate(userId: Int, channelId: Int, codHash: String): Channel

    fun getPublicChannels(sort: Sort?): List<Channel>

    fun updateChannelName(channelId: Int, name: String): Channel

    fun isChannelPublic(channel: Channel): Boolean

    fun leaveChannel(userId: Int, channelId: Int): Boolean

    fun createPrivateInvite(codPrivate: String, userId: Int, channelId: Int, privacy: Int): Int

    fun getMemberPermissions(userId: Int, channelId: Int): Privacy

    fun isInviteCodeValid(userId: Int, channelId: Int, codHash: String): Boolean

    fun channelInviteRejected(userId: Int, channelId: Int, codHash: String)
}
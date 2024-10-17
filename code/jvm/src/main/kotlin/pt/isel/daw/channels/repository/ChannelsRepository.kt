package pt.isel.daw.channels.repository

import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Privacy

interface ChannelsRepository {

    fun createChannel(channel: ChannelModel): Int

    fun isChannelStoredByName(channelName: String): Boolean

    fun isOwnerChannel(channelId: Int, userId: Int): Boolean

    fun getChannelById(channelId: Int): Channel?

    fun getChannelByName(channelName: String): Channel?

    fun getUserOwnedChannels(userId: Int): List<Channel>

    // missing get channels where user is member, not only owner

    fun joinChannel(userId: Int,channelId: Int): Channel

    fun getPublicChannels(): List<Channel>

    fun updateChannelName(channelId: Int, name: String):Channel

    fun isChannelPublic(channel: Channel): Boolean

    fun leaveChannel(channel: Channel, userId: Int): Boolean

    fun createPrivateInvite(codPrivate: String): Int

    fun sendInvitePrivateChannel(userId: Int, channelId: Int, inviteId: Int,privacy: Int) : Int

    fun getTypeInvitePrivateChannel(userId: Int,channelId: Int): Privacy?

    fun isPrivateChannelInviteCodeValid(userId: Int, inviteId: String): Channel?
}
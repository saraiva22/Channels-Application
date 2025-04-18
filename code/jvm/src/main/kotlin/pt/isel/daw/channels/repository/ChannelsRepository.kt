package pt.isel.daw.channels.repository

import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.channels.Sort
import pt.isel.daw.channels.domain.channels.State
import pt.isel.daw.channels.http.model.channel.PrivateInviteOutputModel
import pt.isel.daw.channels.http.model.channel.ChannelUpdateInputModel

interface ChannelsRepository {

    fun createChannel(channel: ChannelModel): Int

    fun isChannelStoredByName(channelName: String): Boolean

    fun isOwnerChannel(channelId: Int, userId: Int): Boolean

    fun getChannelById(channelId: Int): Channel?

    fun searchChannelsByName(channelName: String, sort: Sort?): List<Channel>

    fun getUserOwnedChannels(userId: Int, sort: Sort?): List<Channel>

    fun getAllChannels(sort: Sort?): List<Channel>

    fun joinChannel(userId: Int, channelId: Int): Channel

    fun channelInviteAccepted(userId: Int, channelId: Int, codHash: String): Channel

    fun getPublicChannels(sort: Sort?): List<Channel>

    fun updateChannel(channelId: Int, updateInputModel: ChannelUpdateInputModel): Channel

    fun isChannelPublic(channel: Channel): Boolean

    fun leaveChannel(userId: Int, channelId: Int): Boolean

    fun createPrivateInvite(codPrivate: String, privacy: Int, inviterId: Int, guestId: Int, channelId: Int): Int

    fun getMemberPermissions(guestId: Int, channelId: Int): Privacy?

    fun isInviteCodeValid(userId: Int, channelId: Int, codHash: String): Boolean

    fun channelInviteRejected(userId: Int, channelId: Int, codHash: String)

    fun getReceivedChannelInvites(userId: Int, limit: Int, offSet: Int): List<PrivateInviteOutputModel>

    fun getSentChannelInvites(userId: Int, limit: Int, offSet: Int): List<PrivateInviteOutputModel>

    fun updateChannelUserState(userId: Int, channelId: Int, state: State): Channel
}
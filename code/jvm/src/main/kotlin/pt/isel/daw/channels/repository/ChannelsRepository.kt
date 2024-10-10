package pt.isel.daw.channels.repository

import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel

interface ChannelsRepository {

    fun createChannel(channel: ChannelModel): Int

    fun isChannelStoredByName(channelName: String): Boolean

    fun getChannelById(channelId: Int): Channel?

    fun getChannelByName(channelName: String): Channel?

    fun getUserChannels(userId: Int): List<Channel>

    fun joinChannel(userId: Int): Boolean

    fun getChannels(userId: Int): List<Channel>

    fun getPublicChannels(): List<Channel>

    fun isChannelPublic(channel: Channel): Boolean

    fun leaveChannel(channel: Channel, userId: Int): Boolean
}
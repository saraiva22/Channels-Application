package pt.isel.daw.channels.repository

import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel

interface ChannelsRepository {

    fun createChannel(channel: ChannelModel): Int

    fun isChannelStored(channelName: String): Boolean

    fun getChannelById(channelId: Int): Channel?

    fun joinChannel(userId: Int): Boolean

    fun getChannels(userId: Int): List<Channel>

    fun getPublicChannels(): List<Channel>

    fun leaveChannel(channel: Channel, userId: Int): Boolean
}
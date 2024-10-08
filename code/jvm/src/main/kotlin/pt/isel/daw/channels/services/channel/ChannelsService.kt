package pt.isel.daw.channels.services.channel

import org.springframework.stereotype.Component
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.repository.TransactionManager
import pt.isel.daw.channels.utils.failure
import pt.isel.daw.channels.utils.success

@Component
class ChannelsService(
    private val transactionManager: TransactionManager,
    private val domain: ChannelsDomain
) {
    fun createChannel(channelModel: ChannelModel): ChannelCreationResult {
        return transactionManager.run {
            val channelsRepository = it.channelsRepository
            if (channelsRepository.isChannelStored(channelModel.name)) {
                failure(ChannelCreationError.ChannelAlreadyExists)
            } else {
                val id = channelsRepository.createChannel(channelModel)
                success(id)
            }
        }
    }

    fun getChannelById(id: Int): GetChannelResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(id)
            if (channel == null) {
                failure(GetChannelError.ChannelDoesNotExists)
            } else {
                success(channel)
            }
        }
    }

    fun getPublicChannels(): List<Channel> {
        return transactionManager.run {
            it.channelsRepository.getPublicChannels()
        }
    }
}
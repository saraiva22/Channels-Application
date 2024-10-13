package pt.isel.daw.channels.services.channel

import org.slf4j.LoggerFactory
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
            if (channelsRepository.isChannelStoredByName(channelModel.name)) {
                failure(ChannelCreationError.ChannelAlreadyExists)
            } else {
                val id = channelsRepository.createChannel(channelModel)
                success(id)
            }
        }
    }

    fun getChannelById(channelId: Int): GetChannelResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
            if (channel == null) {
                failure(GetChannelByIdError.ChannelNotFound)
            } else {
                success(channel)
            }
        }
    }

    fun getChannelByName(channelName: String): GetChannelByNameResult = transactionManager.run {
        val channel = it.channelsRepository.getChannelByName(channelName)
            ?: return@run failure(GetChannelByNameError.ChannelNameNotFound)
        success(channel)
    }

    fun getPublicChannels(): List<Channel> {
        return transactionManager.run {
            it.channelsRepository.getPublicChannels()
        }
    }

    fun getUserChannels(userId: Int): List<Channel> {
        return transactionManager.run {
            it.channelsRepository.getUserChannels(userId)
        }
    }

    fun joinUsersInChannel(userId: Int, channelId: Int): GetUserChannelResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getUserChannel(channelId, userId)
            if (channel != null) return@run failure(JoinUserInChannelError.UserAlreadyInChannel)
            val joinChannel = it.channelsRepository.joinChannel(userId, channelId)
            success(joinChannel)
        }
    }

    fun updateNameChannel(nameChannel: String, channelId: Int, userId: Int): UpdateNameChannelResult {
        return transactionManager.run {
            val channelsRepository = it.channelsRepository
            val channel = channelsRepository.getUserChannel(channelId, userId)
            if (channel == null) {
                return@run failure(UpdateNameChannelError.UserNotInChannel)
            } else {
                if (channelsRepository.isChannelStoredByName(nameChannel)) {
                    return@run failure(UpdateNameChannelError.ChannelNameAlreadyExists)
                } else {
                    val newChannel = channelsRepository.updateChannelName(channelId, nameChannel)
                    success(newChannel)
                }
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(ChannelsService::class.java)
    }
}
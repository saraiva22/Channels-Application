package pt.isel.daw.channels.services

import org.springframework.stereotype.Component
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.repository.TransactionManager
import pt.isel.daw.channels.utils.Either
import pt.isel.daw.channels.utils.failure
import pt.isel.daw.channels.utils.success

sealed class ChannelCreationError {
    data object ChannelAlreadyExists: ChannelCreationError()
}

typealias ChannelCreationResult = Either<ChannelCreationError, Int>

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
}
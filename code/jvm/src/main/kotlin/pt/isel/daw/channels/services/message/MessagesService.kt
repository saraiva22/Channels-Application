package pt.isel.daw.channels.services.message

import org.springframework.stereotype.Component
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.repository.TransactionManager
import pt.isel.daw.channels.utils.failure
import pt.isel.daw.channels.utils.success

@Component
class MessagesService(
    private val transactionManager: TransactionManager,
    private val channelsDomain: ChannelsDomain
) {
    fun createMessage(channelId: Int) {
        TODO()
    }

    fun getChannelMessages(userId: Int, channelId: Int): GetMessageResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(GetMessageError.ChannelNotFound)
            if (!it.channelsRepository.isChannelPublic(channel) &&
                !channelsDomain.isUserMember(userId, channel))
                return@run failure(GetMessageError.PermissionDenied)
            val messageList = it.messagesRepository.getChannelMessages(channelId)
            success(messageList)
        }
    }

    fun deleteMessage(channelId: Int) {
        TODO()
    }
}
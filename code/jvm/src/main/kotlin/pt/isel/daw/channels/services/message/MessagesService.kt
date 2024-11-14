package pt.isel.daw.channels.services.message

import jakarta.inject.Named
import kotlinx.datetime.Clock
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.messages.Message
import pt.isel.daw.channels.domain.messages.MessageDomain
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.repository.TransactionManager
import pt.isel.daw.channels.utils.Either
import pt.isel.daw.channels.utils.failure
import pt.isel.daw.channels.utils.success

@Named
class MessagesService(
    private val transactionManager: TransactionManager,
    private val channelsDomain: ChannelsDomain,
    private val messagesDomain: MessageDomain,
    private val chatService: ChatService,
    private val clock: Clock,
) {
    fun createMessage(channelId: Int, user: User, text: String): CreateMessageResult {
        return transactionManager.run {
            val channelRep = it.channelsRepository
            val channel = channelRep.getChannelById(channelId)
                ?: return@run failure(CreateMessageError.ChannelNotFound)
            if (!channelsDomain.isUserMember(user.id, channel))
                return@run failure(CreateMessageError.UserNotMemberInChannel)

            val canCreateMessage = channelRep.isChannelPublic(channel) ||
                    (channelRep.getMemberPermissions(user.id, channelId) == Privacy.READ_WRITE)

            if (canCreateMessage) {
                val now = clock.now()
                val messageId = it.messagesRepository.createMessage(channelId, user.id, text, now)
                chatService.sendMessage(
                    messageId,
                    channelId,
                    user.username,
                    text,
                    channel.members.map { userId -> userId.id })
                success(messageId)
            } else {
                failure(CreateMessageError.PrivacyIsNotReadWrite)
            }
        }
    }

    fun getChannelMessages(userId: Int, channelId: Int): GetMessageResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(GetMessageError.ChannelNotFound)
            if (!channelsDomain.isUserMember(userId, channel))
                return@run failure(GetMessageError.PermissionDenied)
            val messageList = it.messagesRepository.getChannelMessages(channel)
            success(messageList)
        }
    }

    fun deleteMessageFromChannel(userId: Int, messageId: Int, channelId: Int): DeleteMessageResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(DeleteMessageError.ChannelNotFound)
            if (!channelsDomain.isUserMember(userId, channel))
                return@run failure(DeleteMessageError.PermissionDenied)
            val messagesOfChannel = getChannelMessages(userId, channelId)
            if (messagesOfChannel is Either.Right<List<Message>> &&
                !messagesDomain.isMessageInList(messageId, messagesOfChannel.value)) {
                return@run failure(DeleteMessageError.MessageNotFound)
            }
            it.messagesRepository.deleteMessageFromChannel(messageId, channelId)
            success(true)
        }
    }
}
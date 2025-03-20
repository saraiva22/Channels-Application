package pt.isel.daw.channels.services.message

import jakarta.inject.Named
import kotlinx.datetime.Clock
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.messages.MessageDomain
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.domain.user.UserInfo
import pt.isel.daw.channels.repository.TransactionManager
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
                    (channelRep.getMemberPermissions(user.id, channelId) == Privacy.READ_WRITE) ||
                    channelsDomain.isOwner(user.id, channel)

            if (canCreateMessage) {
                val now = clock.now()
                val messageId = it.messagesRepository.createMessage(channelId, user.id, text, now)
                chatService.sendMessage(
                    messageId,
                    text,
                    channel,
                    UserInfo(user.id,user.username,user.email),
                    now.toString()
                )
                success(messageId)
            } else {
                failure(CreateMessageError.PrivacyIsNotReadWrite)
            }
        }
    }

    fun getChannelMessages(userId: Int, channelId: Int): GetMessagesResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(GetMessagesError.ChannelNotFound)

            if (!channelsDomain.isOwner(userId, channel) && !channelsDomain.isUserMember(userId, channel))
                return@run failure(GetMessagesError.PermissionDenied)

            val messageList = it.messagesRepository.getChannelMessages(channel)
            success(messageList)
        }
    }


    fun getMessageById(userId: Int, messageId: Int, channelId: Int): GetMessageResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(GetMessageError.ChannelNotFound)

            if (!channelsDomain.isUserMember(userId, channel))
                return@run failure(GetMessageError.PermissionDenied)

            val message = it.messagesRepository.getMessageById(messageId, channel) ?: return@run failure(
                GetMessageError.MessageNotFound
            )
            success(message)
        }
    }

    fun deleteMessageFromChannel(userId: Int, messageId: Int, channelId: Int): DeleteMessageResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(DeleteMessageError.ChannelNotFound)

            if (!channelsDomain.isUserMember(userId, channel))
                return@run failure(DeleteMessageError.PermissionDenied)

            val message = it.messagesRepository.getMessageById(messageId, channel) ?: return@run failure(
                DeleteMessageError.MessageNotFound
            )

            if (!channelsDomain.isOwner(userId, channel) && !messagesDomain.isCreatorOfMessage(userId, message))
                return@run failure(DeleteMessageError.PermissionDenied)

            it.messagesRepository.deleteMessageFromChannel(messageId, channelId)
            success(true)
        }
    }

}
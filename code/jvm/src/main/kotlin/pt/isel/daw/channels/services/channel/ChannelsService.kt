package pt.isel.daw.channels.services.channel

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.http.model.channel.RegisterPrivateInviteModel
import pt.isel.daw.channels.repository.Transaction
import pt.isel.daw.channels.repository.TransactionManager
import pt.isel.daw.channels.utils.Either
import pt.isel.daw.channels.utils.failure
import pt.isel.daw.channels.utils.success

@Component
class ChannelsService(
    private val transactionManager: TransactionManager,
    private val channelsDomain: ChannelsDomain
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

    fun getChannelById(userId: Int, channelId: Int): GetChannelResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(GetChannelByIdError.ChannelNotFound)
            if (!it.channelsRepository.isChannelPublic(channel) &&
                !channelsDomain.isUserMember(userId, channel))
                return@run failure(GetChannelByIdError.PermissionDenied)
            success(channel)
        }
    }

    fun getChannelByName(userId: Int, channelName: String): GetChannelByNameResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelByName(channelName)
                ?: return@run failure(GetChannelByNameError.ChannelNameNotFound)
            if (!it.channelsRepository.isChannelPublic(channel) &&
                !channelsDomain.isUserMember(userId, channel))
                return@run failure(GetChannelByNameError.PermissionDenied)
            success(channel)
        }
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

    fun getUserChannel(channelId: Int, userId: Int): Channel? =
        transactionManager.run { it.channelsRepository.getUserChannel(channelId, userId) }

    fun joinUsersInPublicChannel(userId: Int, channelId: Int): JoinUserInChannelPublicResult {
        return transactionManager.run {
            if (it.channelsRepository.getChannelById(channelId) == null) return@run failure(JoinUserInChannelPublicError.ChannelNotFound)

            val channel = it.channelsRepository.getUserChannel(channelId, userId)
            if (channel != null) return@run failure(JoinUserInChannelPublicError.UserAlreadyInChannel)

            val joinChannel = it.channelsRepository.joinChannel(userId, channelId)
            success(joinChannel)
        }
    }

    fun joinUsersInPrivateChannel(userId: Int, channelId: Int, codInvite: String): JoinUserInChannelPrivateResult {
        return transactionManager.run {
            if (it.channelsRepository.getChannelById(channelId) == null) return@run failure(
                JoinUserInChannelPrivateError.ChannelNotFound
            )
            val channel = it.channelsRepository.getUserChannel(channelId, userId)
            if (channel != null) return@run failure(JoinUserInChannelPrivateError.UserAlreadyInChannel)
            if (it.channelsRepository.isPrivateChannelInviteCodeValid(userId, channelId, codInvite)) {
                val joinChannel = it.channelsRepository.joinChannel(userId, channelId)
                success(joinChannel)
            } else {
                return@run failure(JoinUserInChannelPrivateError.CodeInvalid)
            }
        }
    }


    fun invitePrivateChannel(
        channel: Channel,
        userId: Int,
        guest: User,
        privacy: Privacy
    ): InvitePrivateChannelResult {
        return transactionManager.run {
            if (it.channelsRepository.getUserChannel(channel.id, guest.id) != null) {
                return@run failure(InvitePrivateChannelError.UserAlreadyInChannel)
            }
            val isOwner = it.channelsRepository.isOwnerChannel(channel.id, userId)
            if (isOwner) {
                createInvite(it, guest.id, channel.id, privacy)
            } else {
                if (channel.members.contains(userId)) {
                    val typeInvite = it.channelsRepository.getTypeInvitePrivateChannel(userId, channel.id)
                    if (typeInvite == Privacy.READ_WRITE) {
                        createInvite(it, guest.id, channel.id, privacy)
                    } else if (typeInvite == Privacy.READ_ONLY && privacy == Privacy.READ_ONLY) {
                        createInvite(it, guest.id, channel.id, privacy)
                    } else {
                        return@run failure(InvitePrivateChannelError.UserNotPermissionsType)
                    }
                } else {
                    return@run failure(InvitePrivateChannelError.UserNotInChannel)
                }
            }
        }
    }

    private fun createInvite(
        it: Transaction,
        userId: Int,
        channelId: Int,
        privacy: Privacy
    ): Either.Right<String> {
        val codHash = channelsDomain.generateInvitation()
        val register = RegisterPrivateInviteModel(codHash)
        val inviteId = it.channelsRepository.createPrivateInvite(codHash)
        it.channelsRepository.sendInvitePrivateChannel(userId, channelId, inviteId, privacy.ordinal)
        return success(register.codHash)
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
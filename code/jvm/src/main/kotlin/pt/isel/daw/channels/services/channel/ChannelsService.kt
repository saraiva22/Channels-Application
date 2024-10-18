package pt.isel.daw.channels.services.channel

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.domain.channels.Privacy
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
                !channelsDomain.isUserMember(userId, channel)
            )
                return@run failure(GetChannelByIdError.PermissionDenied)
            success(channel)
        }
    }

    fun getChannelByName(userId: Int, channelName: String): GetChannelByNameResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelByName(channelName)
                ?: return@run failure(GetChannelByNameError.ChannelNameNotFound)
            if (!it.channelsRepository.isChannelPublic(channel) &&
                !channelsDomain.isUserMember(userId, channel)
            )
                return@run failure(GetChannelByNameError.PermissionDenied)
            success(channel)
        }
    }

    fun getPublicChannels(): List<Channel> {
        return transactionManager.run {
            it.channelsRepository.getPublicChannels()
        }
    }

    fun getUserOwnedChannels(userId: Int): List<Channel> {
        return transactionManager.run {
            it.channelsRepository.getUserOwnedChannels(userId)
        }
    }


    fun joinUsersInPublicChannel(userId: Int, channelId: Int): JoinUserInChannelPublicResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(JoinUserInChannelPublicError.ChannelNotFound)
            if (!it.channelsRepository.isChannelPublic(channel))
                return@run failure(JoinUserInChannelPublicError.IsPrivateChannel)

            if (channelsDomain.isUserMember(userId, channel))
                return@run failure(JoinUserInChannelPublicError.UserAlreadyInChannel)

            val joinChannel = it.channelsRepository.joinChannel(userId, channelId)
            success(joinChannel)
        }
    }

    fun joinUsersInPrivateChannel(userId: Int, channelId: Int, codInvite: String): JoinUserInChannelPrivateResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(JoinUserInChannelPrivateError.ChannelNotFound)

            if (channelsDomain.isOwner(userId, channel)) {
                if (channelsDomain.isUserMember(userId, channel)) {
                    return@run failure(JoinUserInChannelPrivateError.UserAlreadyInChannel)
                }
                val joinChannel = it.channelsRepository.joinChannel(userId, channel.id)
                return@run success(joinChannel)
            }

            if (channelsDomain.isUserMember(userId, channel))
                return@run failure(JoinUserInChannelPrivateError.UserAlreadyInChannel)

            val validChannel =
                it.channelsRepository.isPrivateChannelInviteCodeValid(userId, channelId, codInvite, false)
                    ?: return@run failure(JoinUserInChannelPrivateError.CodeInvalidOrExpired)

            val joinChannel = it.channelsRepository.joinMemberInChannelPrivate(userId, validChannel.id, codInvite)
            success(joinChannel)

        }
    }


    fun invitePrivateChannel(
        channelId: Int,
        userId: Int,
        guestName: String,
        privacy: Privacy
    ): InvitePrivateChannelResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(InvitePrivateChannelError.ChannelNotFound)

            val guestUser = it.usersRepository.getUserByUsername(guestName)
                ?: return@run failure(InvitePrivateChannelError.GuestNotFound)

            if (channelsDomain.isUserMember(guestUser.id, channel)) {
                return@run failure(InvitePrivateChannelError.UserAlreadyInChannel)
            }

            if (it.channelsRepository.isChannelPublic(channel)) {
                return@run failure(InvitePrivateChannelError.ChannelIsPublic)
            }

            val isOwner = channelsDomain.isOwner(userId, channel)
            if (isOwner) {
                createInvite(it, guestUser.id, channel.id, privacy)
            } else {
                if (channelsDomain.isUserMember(userId, channel)) {
                    val typeInvite =
                        it.channelsRepository.getTypeInvitePrivateChannel(userId, channel.id)
                            ?: return@run failure(InvitePrivateChannelError.PrivacyTypeNotFound)
                    if (typeInvite == Privacy.READ_WRITE) {
                        createInvite(it, guestUser.id, channel.id, privacy)
                    } else if (typeInvite == Privacy.READ_ONLY && privacy == Privacy.READ_ONLY) {
                        createInvite(it, guestUser.id, channel.id, privacy)
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
        val inviteId = it.channelsRepository.createPrivateInvite(register.codHash, false)
        it.channelsRepository.sendInvitePrivateChannel(userId, channelId, inviteId, privacy.ordinal)
        return success(register.codHash)
    }


    fun updateNameChannel(nameChannel: String, channelId: Int, userId: Int): UpdateNameChannelResult {
        return transactionManager.run {
            val channelsRepository = it.channelsRepository
            val channel = channelsRepository.getChannelById(channelId)
                ?: return@run failure(UpdateNameChannelError.ChannelNotFound)

            if (channelsDomain.isOwner(userId, channel) || channelsDomain.isUserMember(userId, channel)) {

                if (channelsRepository.isChannelStoredByName(nameChannel)) {
                    return@run failure(UpdateNameChannelError.ChannelNameAlreadyExists)
                }

                val newChannel = channelsRepository.updateChannelName(channelId, nameChannel)
                return@run success(newChannel)
            } else {
                return@run failure(UpdateNameChannelError.UserNotInChannel)
            }
        }
    }

    fun leaveChannel(userId: Int, channelId: Int): LeaveChannelResult {
        return transactionManager.run {
            val channelsRepository = it.channelsRepository
            val channel = channelsRepository.getChannelById(channelId)
                ?: return@run failure(LeaveChannelResultError.ChannelNotFound)

            if (!channelsDomain.isUserMember(userId, channel)) {
                return@run failure(LeaveChannelResultError.UserNotInChannel)
            }
            if (!channelsRepository.leaveChannel(userId, channelId))
                failure(LeaveChannelResultError.ErrorLeavingChannel)
            success(Unit)

        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(ChannelsService::class.java)
    }
}
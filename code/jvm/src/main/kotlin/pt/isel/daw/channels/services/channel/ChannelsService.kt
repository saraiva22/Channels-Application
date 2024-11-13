package pt.isel.daw.channels.services.channel

import org.springframework.stereotype.Component
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.channels.Sort
import pt.isel.daw.channels.domain.channels.Status
import pt.isel.daw.channels.http.model.channel.RegisterPrivateInviteInputModel
import pt.isel.daw.channels.repository.Transaction
import pt.isel.daw.channels.repository.TransactionManager
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

    fun getChannelByName(userId: Int, channelName: String, sort: Sort?): List<Channel> {
        return transactionManager.run {
            val channelsList = it.channelsRepository.searchChannelsByName(channelName, sort)

            channelsList.filter {
                channel -> it.channelsRepository.isChannelPublic(channel) ||
                    channelsDomain.isUserMember(userId, channel)
            }
        }
    }

    fun getPublicChannels(sort: Sort?): List<Channel> {
        return transactionManager.run {
            it.channelsRepository.getPublicChannels(sort)
        }
    }

    fun getUserOwnedChannels(userId: Int, sort: Sort?): List<Channel> {
        return transactionManager.run {
            it.channelsRepository.getUserOwnedChannels(userId, sort)
        }
    }

    fun getUserMemberChannels(userId: Int, sort: Sort?): List<Channel> {
        return transactionManager.run {
            val channels = it.channelsRepository.getAllChannels(sort)
            channels.filter { channel -> channelsDomain.isUserMember(userId, channel) }
        }
    }

    fun joinUsersInPublicChannel(userId: Int, channelId: Int): JoinUserInChannelPublicResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(JoinUserInChannelPublicError.ChannelNotFound)
            if (!it.channelsRepository.isChannelPublic(channel))
                return@run failure(JoinUserInChannelPublicError.ChannelIsPrivate)

            if (channelsDomain.isUserMember(userId, channel))
                return@run failure(JoinUserInChannelPublicError.UserAlreadyInChannel)

            val joinChannel = it.channelsRepository.joinChannel(userId, channelId)
            success(joinChannel)
        }
    }

    fun joinUsersInPrivateChannel(
        userId: Int,
        channelId: Int,
        codInvite: String,
        status: Status
    ): JoinUserInChannelPrivateResult {
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

            if (!it.channelsRepository.isInviteCodeValid(userId, channelId, codInvite))
                return@run failure(JoinUserInChannelPrivateError.InvalidCode)

            if (status == Status.ACCEPT) {
                val joinChannel = it.channelsRepository.joinMemberInChannelPrivate(userId, channelId, codInvite)
                return@run success(joinChannel)
            } else {
                it.channelsRepository.channelInviteRejected(userId, channelId, codInvite)
                return@run failure(JoinUserInChannelPrivateError.InviteRejected)
            }
        }
    }


    fun invitePrivateChannel(
        inviteModel: RegisterPrivateInviteInputModel
    ): InvitePrivateChannelResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(inviteModel.channelId)
                ?: return@run failure(InvitePrivateChannelError.ChannelNotFound)

            val guestUser = it.usersRepository.getUserByUsername(inviteModel.guestName)
                ?: return@run failure(InvitePrivateChannelError.GuestNotFound)

            if (channelsDomain.isUserMember(guestUser.id, channel)) {
                return@run failure(InvitePrivateChannelError.UserAlreadyInChannel)
            }

            if (it.channelsRepository.isChannelPublic(channel)) {
                return@run failure(InvitePrivateChannelError.ChannelIsPublic)
            }

            val isOwner = channelsDomain.isOwner(inviteModel.userId, channel)
            if (isOwner) {
                success(createInvite(it, guestUser.id, channel.id, inviteModel.inviteType))
            } else {
                if (channelsDomain.isUserMember(inviteModel.userId, channel)) {
                    val guestPermission =
                        it.channelsRepository.getMemberPermissions(inviteModel.userId, channel.id)
                    if (guestPermission == Privacy.READ_WRITE) {
                        success(createInvite(it, guestUser.id, channel.id, inviteModel.inviteType))
                    } else if (guestPermission == Privacy.READ_ONLY && inviteModel.inviteType == Privacy.READ_ONLY) {
                        success(createInvite(it, guestUser.id, channel.id, inviteModel.inviteType))
                    } else {
                        return@run failure(InvitePrivateChannelError.UserPermissionsDeniedType)
                    }
                } else {
                    return@run failure(InvitePrivateChannelError.UserNotInChannel)
                }
            }
        }
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

    fun dbHasUsers(): Boolean {
        return transactionManager.run {
            it.usersRepository.hasUsers()
        }
    }

    private fun createInvite(it: Transaction, userId: Int, channelId: Int, privacy: Privacy): String {
        val inviteLink = channelsDomain.generateInvitation(channelId)
        val inviteCode = inviteLink.split("/").last()
        it.channelsRepository.createPrivateInvite(inviteCode, userId, channelId, privacy.ordinal)
        return inviteLink
    }
}
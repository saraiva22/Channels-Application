package pt.isel.daw.channels.services.channel

import jakarta.inject.Named
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.channels.Sort
import pt.isel.daw.channels.domain.channels.State
import pt.isel.daw.channels.domain.channels.Status
import pt.isel.daw.channels.http.model.channel.PrivateInviteOutputModel
import pt.isel.daw.channels.http.model.channel.ChannelUpdateInputModel
import pt.isel.daw.channels.http.model.channel.RegisterPrivateInviteInputModel
import pt.isel.daw.channels.repository.Transaction
import pt.isel.daw.channels.repository.TransactionManager
import pt.isel.daw.channels.utils.failure
import pt.isel.daw.channels.utils.success

@Named
class ChannelsService(
    private val transactionManager: TransactionManager,
    private val channelsDomain: ChannelsDomain
) {
    fun createChannel(channelModel: ChannelModel): ChannelCreationResult {
        return transactionManager.run {
            val channelsRepository = it.channelsRepository
            if (channelsRepository.isChannelStoredByName(channelModel.name)) {
                return@run failure(ChannelCreationError.ChannelAlreadyExists)
            }
            val id = channelsRepository.createChannel(channelModel)
            success(id)
        }
    }

    fun getChannelById(userId: Int, channelId: Int): GetChannelResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(GetChannelByIdError.ChannelNotFound)
            if (!it.channelsRepository.isChannelPublic(channel) && !channelsDomain.isUserMember(userId, channel) &&
                channelsDomain.isUserBanned(userId, channel)
            ) return@run failure(GetChannelByIdError.PermissionDenied)

            success(channel)
        }
    }

    fun searchChannelsByName(userId: Int, channelName: String, sort: Sort?): List<Channel> {
        return transactionManager.run {
            val channelsList = it.channelsRepository.searchChannelsByName(channelName, sort)

            channelsList.filter { channel ->
                it.channelsRepository.isChannelPublic(channel) ||
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
            channels.filter { channel ->
                channelsDomain.isUserMember(userId, channel) &&
                        !channelsDomain.isUserBanned(userId, channel)
            }
        }
    }

    fun joinUsersInPublicChannel(userId: Int, channelId: Int): JoinUserInChannelPublicResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(JoinUserInChannelPublicError.ChannelNotFound)

            if (!it.channelsRepository.isChannelPublic(channel))
                return@run failure(JoinUserInChannelPublicError.ChannelIsPrivate)

            if (channelsDomain.isUserBanned(userId, channel))
                return@run failure(JoinUserInChannelPublicError.UserIsBanned)

            if (channelsDomain.isUserMember(userId, channel))
                return@run failure(JoinUserInChannelPublicError.UserAlreadyInChannel)

            val joinChannel = it.channelsRepository.joinChannel(userId, channelId)
            success(joinChannel)
        }
    }

    fun validateChannelInvite(
        guestId: Int,
        channelId: Int,
        codInvite: String,
        status: Status
    ): ValidateChannelInviteResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(ValidateChannelInviteError.ChannelNotFound)

            if (channelsDomain.isUserBanned(guestId, channel))
                return@run failure(ValidateChannelInviteError.GuestIsBanned)

            // Check this logic
            if (channelsDomain.isOwner(guestId, channel)) {
                if (channelsDomain.isUserMember(guestId, channel)) {
                    return@run failure(ValidateChannelInviteError.UserAlreadyInChannel)
                }
                val joinChannel = it.channelsRepository.joinChannel(guestId, channel.id)
                return@run success(joinChannel)
            }

            if (channelsDomain.isUserMember(guestId, channel))
                return@run failure(ValidateChannelInviteError.UserAlreadyInChannel)

            if (!it.channelsRepository.isInviteCodeValid(guestId, channelId, codInvite))
                return@run failure(ValidateChannelInviteError.InvalidCode)

            if (status == Status.ACCEPT) {
                val joinChannel = it.channelsRepository.channelInviteAccepted(guestId, channelId, codInvite)
                return@run success(joinChannel)
            } else {
                it.channelsRepository.channelInviteRejected(guestId, channelId, codInvite)
                return@run failure(ValidateChannelInviteError.InviteRejected)
            }
        }
    }


    fun updateChannel(
        updateInputModel: ChannelUpdateInputModel,
        channelId: Int,
        userId: Int
    ): UpdateChannelResult {
        return transactionManager.run {
            val channelsRepository = it.channelsRepository
            val channel = channelsRepository.getChannelById(channelId)
                ?: return@run failure(UpdateChannelError.ChannelNotFound)


            if (channelsDomain.isOwner(userId, channel)) {
                if (updateInputModel.name != null && channelsRepository.isChannelStoredByName(updateInputModel.name)) {
                    return@run failure(UpdateChannelError.ChannelNameAlreadyExists)
                }

                val updatedChannel = channelsRepository.updateChannel(channelId, updateInputModel)
                return@run success(updatedChannel)
            } else {
                return@run failure(UpdateChannelError.UserNotChannelOwner)
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

    fun getReceivedChannelInvites(userId: Int, limit: Int, offSet: Int): List<PrivateInviteOutputModel> {
        return transactionManager.run {
            it.channelsRepository.getReceivedChannelInvites(userId, limit, offSet)
        }
    }

    fun getSentChannelInvites(userId: Int, limit: Int, offSet: Int): List<PrivateInviteOutputModel> {
        return transactionManager.run {
            it.channelsRepository.getSentChannelInvites(userId, limit, offSet)
        }
    }

    fun banUserFromChannel(ownerId: Int, userName: String, channelId: Int): BanUserResult {
        return transactionManager.run {
            val userToBan = it.usersRepository.getUserByUsername(userName)
                ?: return@run failure(BanUserResultError.UsernameNotFound)

            val channelsRepository = it.channelsRepository
            val channel = channelsRepository.getChannelById(channelId)
                ?: return@run failure(BanUserResultError.ChannelNotFound)

            if (!channelsDomain.isOwner(ownerId, channel)) {
                return@run failure(BanUserResultError.UserIsNotOwner)
            }

            if (channelsDomain.isOwner(userToBan.id, channel)) {
                return@run failure(BanUserResultError.OwnerNotBanned)
            }

            if (channelsDomain.isUserBanned(userToBan.id, channel)) {
                return@run failure(BanUserResultError.UserAlreadyBanned)
            }

            if (!channelsDomain.isUserMember(userToBan.id, channel)) {
                return@run failure(BanUserResultError.UserNotInChannel)
            } else {
                val newChannel = channelsRepository.updateChannelUserState(userToBan.id, channel.id, State.BANNED)
                return@run success(newChannel)
            }
        }
    }

    fun unbanUserFromChannel(ownerId: Int, userName: String, channelId: Int): UnbanUserResult {
        return transactionManager.run {
            val userToUnban = it.usersRepository.getUserByUsername(userName)
                ?: return@run failure(UnbanUserResultError.UsernameNotFound)

            val channelsRepository = it.channelsRepository
            val channel = channelsRepository.getChannelById(channelId)
                ?: return@run failure(UnbanUserResultError.ChannelNotFound)

            if (!channelsDomain.isOwner(ownerId, channel)) {
                return@run failure(UnbanUserResultError.UserIsNotOwner)
            }

            if (channelsDomain.isOwner(userToUnban.id, channel)) {
                return@run failure(UnbanUserResultError.OwnerNotBanned)
            }

            if (!channelsDomain.isUserBanned(userToUnban.id, channel)) {
                return@run failure(UnbanUserResultError.UserIsNotBanned)
            }

            val newChannel = channelsRepository.updateChannelUserState(userToUnban.id, channel.id, State.UNBANNED)
            return@run success(newChannel)
        }
    }

    fun invitePrivateChannel(
        inviteModel: RegisterPrivateInviteInputModel
    ): InvitePrivateChannelResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(inviteModel.channelId)
                ?: return@run failure(InvitePrivateChannelError.ChannelNotFound)

            if (it.channelsRepository.isChannelPublic(channel)) {
                return@run failure(InvitePrivateChannelError.ChannelIsPublic)
            }

            if (!channelsDomain.isOwner(
                    inviteModel.inviterId,
                    channel
                ) && !channelsDomain.isUserMember(inviteModel.inviterId, channel)
            ) {
                return@run failure(InvitePrivateChannelError.UserNotInChannel)
            }

            val guestUser = it.usersRepository.getUserByUsername(inviteModel.guestName)
                ?: return@run failure(InvitePrivateChannelError.GuestNotFound)

            if (channelsDomain.isUserBanned(guestUser.id, channel)) {
                return@run failure(InvitePrivateChannelError.GuestIsBanned)
            }

            if (channelsDomain.isUserMember(guestUser.id, channel)) {
                return@run failure(InvitePrivateChannelError.UserAlreadyInChannel)
            }

            if (isUserAuthorizedToInvite(it, inviteModel.inviterId, channel, inviteModel.inviteType)) {
                success(createInvite(it, inviteModel.inviterId, guestUser.id, channel.id, inviteModel.inviteType))
            } else {
                failure(InvitePrivateChannelError.UserPermissionsDeniedType)
            }
        }
    }

    private fun isUserAuthorizedToInvite(
        transaction: Transaction,
        inviterId: Int,
        channel: Channel,
        inviteType: Privacy
    ): Boolean {
        return if (channelsDomain.isOwner(inviterId, channel)) {
            true
        } else {
            val guestPermission = transaction.channelsRepository.getMemberPermissions(inviterId, channel.id)
            guestPermission ==
                    Privacy.READ_WRITE ||
                    (guestPermission == Privacy.READ_ONLY && inviteType == Privacy.READ_ONLY)
        }
    }

    private fun createInvite(
        transaction: Transaction,
        inviterId: Int,
        guestId: Int,
        channelId: Int,
        privacy: Privacy
    ): String {
        val inviteCode = channelsDomain.generateInvitation(channelId)
        transaction.channelsRepository.createPrivateInvite(inviteCode, privacy.ordinal, inviterId, guestId, channelId)
        return inviteCode
    }
}
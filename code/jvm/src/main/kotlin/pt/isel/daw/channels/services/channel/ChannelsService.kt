package pt.isel.daw.channels.services.channel

import jakarta.inject.Named
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

@Named
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
        guestId: Int,
        channelId: Int,
        codInvite: String,
        status: Status
    ): JoinUserInChannelPrivateResult {
        return transactionManager.run {
            val channel = it.channelsRepository.getChannelById(channelId)
                ?: return@run failure(JoinUserInChannelPrivateError.ChannelNotFound)

            if (channelsDomain.isOwner(guestId, channel)) {
                if (channelsDomain.isUserMember(guestId, channel)) {
                    return@run failure(JoinUserInChannelPrivateError.UserAlreadyInChannel)
                }
                val joinChannel = it.channelsRepository.joinChannel(guestId, channel.id)
                return@run success(joinChannel)
            }

            if (channelsDomain.isUserMember(guestId, channel))
                return@run failure(JoinUserInChannelPrivateError.UserAlreadyInChannel)

            if (!it.channelsRepository.isInviteCodeValid(guestId, channelId, codInvite))
                return@run failure(JoinUserInChannelPrivateError.InvalidCode)

            if (status == Status.ACCEPT) {
                println("in2")
                val joinChannel = it.channelsRepository.joinMemberInChannelPrivate(guestId, channelId, codInvite)
                return@run success(joinChannel)
            } else {
                it.channelsRepository.channelInviteRejected(guestId, channelId, codInvite)
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

            if (!channelsDomain.isUserMember(inviteModel.inviterId, channel)) {
                return@run failure(InvitePrivateChannelError.UserNotInChannel)
            }

            val guestUser = it.usersRepository.getUserByUsername(inviteModel.guestName)
                ?: return@run failure(InvitePrivateChannelError.GuestNotFound)

            println(guestUser.id)
            println(channelsDomain.isUserMember(guestUser.id, channel))
            if (channelsDomain.isUserMember(guestUser.id, channel)) {
                println("in")
                return@run failure(InvitePrivateChannelError.UserAlreadyInChannel)
            }

            if (it.channelsRepository.isChannelPublic(channel)) {
                return@run failure(InvitePrivateChannelError.ChannelIsPublic)
            }

            if (isUserAuthorizedToInvite(it, inviteModel.inviterId, channel, inviteModel.inviteType)) {
                success(createInvite(it, inviteModel.inviterId, guestUser.id, channel.id, inviteModel.inviteType))
            } else {
                failure(InvitePrivateChannelError.UserPermissionsDeniedType)
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
        transaction : Transaction,
        inviterId: Int,
        guestId: Int,
        channelId: Int,
        privacy: Privacy
    ): String {
        val inviteLink = channelsDomain.generateInvitation(channelId)
        val inviteCode = inviteLink.split("/").last()
        transaction.channelsRepository.createPrivateInvite(inviteCode, privacy.ordinal, inviterId, guestId, channelId)
        return inviteLink
    }
}
package pt.isel.daw.channels.http.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Sort
import pt.isel.daw.channels.domain.user.AuthenticatedUser
import pt.isel.daw.channels.http.Uris
import pt.isel.daw.channels.http.media.Problem
import pt.isel.daw.channels.http.model.channel.*
import pt.isel.daw.channels.http.model.utils.IdOutputModel
import pt.isel.daw.channels.services.channel.*
import pt.isel.daw.channels.utils.Failure
import pt.isel.daw.channels.utils.Success


@RestController
class ChannelsController(
    private val channelsService: ChannelsService
) {

    companion object {
        private const val DEFAULT_LIMIT = 10
        private const val DEFAULT_OFFSET = 0
    }

    @PostMapping(Uris.Channels.CREATE)
    fun createChannel(
        @RequestBody input: ChannelCreateInputModel,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.register()
        val userId = authenticatedUser.user.id
        val channel = ChannelModel(input.name, userId, input.type)
        return when (val res = channelsService.createChannel(channel)) {
            is Success -> ResponseEntity
                .status(201)
                .header(
                    "Location",
                    Uris.Channels.byId(res.value).toASCIIString()
                )
                .body(IdOutputModel(res.value))

            is Failure -> when (res.value) {
                ChannelCreationError.ChannelAlreadyExists -> Problem.channelAlreadyExists(instance)
            }
        }
    }

    @GetMapping(Uris.Channels.GET_PUBLIC_CHANNELS)
    fun getPublicChannels(
        @RequestParam sort: String?,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) offset: Int?,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val setLimit = limit ?: DEFAULT_LIMIT
        val setOffset = offset ?: DEFAULT_OFFSET
        val sortParam = sort?.let { Sort.fromString(sort) }
        val res = channelsService.getPublicChannels(sortParam)
        return ResponseEntity
            .status(200)
            .body(ChannelsListOutputModel(res))
    }

    @GetMapping(Uris.Channels.GET_BY_ID)
    fun getChannelById(
        @PathVariable id: Int,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.byId(id)
        val userId = authenticatedUser.user.id
        return when (val res = channelsService.getChannelById(userId, id)) {
            is Success -> ResponseEntity
                .status(200)
                .body(
                    ChannelOutputModel(
                        res.value.id,
                        res.value.name,
                        res.value.owner,
                        res.value.type,
                        res.value.members,
                        res.value.bannedMembers
                    )
                )

            is Failure -> when (res.value) {
                GetChannelByIdError.ChannelNotFound -> Problem.channelNotFound(id, instance)
                GetChannelByIdError.PermissionDenied -> Problem.userPermissionsDenied(instance)
            }
        }
    }

    @GetMapping(Uris.Channels.GET_BY_NAME)
    fun searchChannelsByName(
        @RequestParam name: String,
        @RequestParam sort: String?,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val userId = authenticatedUser.user.id
        val sortParam = sort?.let { Sort.fromString(sort) }
        val res = channelsService.searchChannelsByName(userId, name, sortParam)
        return ResponseEntity
            .status(200)
            .body(ChannelsListOutputModel(res))
    }

    @GetMapping(Uris.Channels.GET_USER_OWNED_CHANNELS)
    fun getUserOwnedChannels(
        @RequestParam sort: String?,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val userId = authenticatedUser.user.id
        val sortParam = Sort.fromString(sort ?: "")
        val res = channelsService.getUserOwnedChannels(userId, sortParam)
        return ResponseEntity
            .status(200)
            .body(ChannelsListOutputModel(res))
    }

    @GetMapping(Uris.Channels.GET_USER_MEMBER_CHANNELS)
    fun getUserMemberChannels(
        @RequestParam sort: String?,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val userId = authenticatedUser.user.id
        val sortParam = sort?.let { Sort.fromString(sort) }
        val res = channelsService.getUserMemberChannels(userId, sortParam)
        return ResponseEntity
            .status(200)
            .body(ChannelsListOutputModel(res))
    }

    @PatchMapping(Uris.Channels.UPDATE)
    fun updateChannel(
        @PathVariable id: Int,
        @RequestBody updateInput: ChannelUpdateInputModel,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.update(id)
        val userId = authenticatedUser.user.id
        return when (val updateChannel = channelsService.updateChannel(updateInput, id, userId)) {
            is Success ->
                ResponseEntity
                    .status(200)
                    .body(
                        ChannelOutputModel(
                            updateChannel.value.id,
                            updateChannel.value.name,
                            updateChannel.value.owner,
                            updateChannel.value.type,
                            updateChannel.value.members,
                            updateChannel.value.bannedMembers
                        )
                    )

            is Failure -> {
                when (updateChannel.value) {
                    UpdateChannelError.UserNotInChannel -> Problem.userNotInChannel(
                        authenticatedUser.user.username,
                        instance
                    )

                    UpdateChannelError.UserNotChannelOwner -> Problem.userIsNotChannelOwner(
                        authenticatedUser.user.username,
                        instance
                    )

                    UpdateChannelError.ChannelNameAlreadyExists -> Problem.channelNameAlreadyExists(
                        updateInput.name ?: "null",
                        instance
                    )

                    UpdateChannelError.ChannelNotFound -> Problem.channelNotFound(
                        id,
                        instance
                    )
                }
            }
        }
    }

    @PostMapping(Uris.Channels.JOIN_PUBLIC_CHANNELS)
    fun joinPublicChannel(
        @PathVariable id: Int,
        userAuthenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.joinPublicChannel(id)
        return when (
            val channel = channelsService.joinUsersInPublicChannel(userAuthenticatedUser.user.id, id)
        ) {
            is Success -> ResponseEntity
                .status(200)
                .body(
                    ChannelOutputModel(
                        channel.value.id,
                        channel.value.name,
                        channel.value.owner,
                        channel.value.type,
                        channel.value.members,
                        channel.value.bannedMembers
                    )
                )

            is Failure -> when (channel.value) {
                JoinUserInChannelPublicError.UserAlreadyInChannel -> Problem.userAlreadyInChannel(
                    userAuthenticatedUser.user.username,
                    instance
                )

                JoinUserInChannelPublicError.ChannelNotFound -> Problem.channelNotFound(id, instance)

                JoinUserInChannelPublicError.ChannelIsPrivate -> Problem.channelIsPrivate(id, instance)

                JoinUserInChannelPublicError.UserIsBanned -> Problem.userIsBanned(
                    userAuthenticatedUser.user.username,
                    id,
                    instance
                )
            }
        }

    }


    @PostMapping(Uris.Channels.VALIDATE_CHANNEL_INVITE)
    fun validateChannelInvite(
        @PathVariable id: Int,
        @PathVariable code: String,
        @RequestBody status: ChannelInviteStatusInputModel,
        userAuthenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.validateChannelInvite(id, code)
        return when (
            val channel = channelsService.validateChannelInvite(
                userAuthenticatedUser.user.id,
                id,
                code,
                status.status
            )
        ) {
            is Success -> ResponseEntity
                .status(200)
                .body(
                    ChannelOutputModel(
                        channel.value.id,
                        channel.value.name,
                        channel.value.owner,
                        channel.value.type,
                        channel.value.members,
                        channel.value.bannedMembers
                    )
                )

            is Failure -> when (channel.value) {
                ValidateChannelInviteError.GuestIsBanned -> Problem.userIsBanned(
                    userAuthenticatedUser.user.username,
                    id,
                    instance
                )

                ValidateChannelInviteError.UserAlreadyInChannel -> Problem.userAlreadyInChannel(
                    userAuthenticatedUser.user.username,
                    instance
                )

                ValidateChannelInviteError.InvalidCode -> Problem.codeInvalidOrExpiredChannel(
                    code,
                    instance
                )

                ValidateChannelInviteError.ChannelNotFound -> Problem.channelNotFound(id, instance)
                ValidateChannelInviteError.InviteRejected -> ResponseEntity.status(200).build<Unit>()
            }
        }
    }

    @PostMapping(Uris.Channels.CREATE_PRIVATE_INVITE)
    fun invitePrivateChannel(
        @PathVariable id: Int,
        @RequestBody input: ChannelPrivateInviteInput,
        authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val instance = Uris.Channels.invitePrivateChannel(id)
        val channelPrivate = channelsService.invitePrivateChannel(
            RegisterPrivateInviteInputModel(
                id,
                authenticatedUser.user.id,
                input.username,
                input.privacy
            )
        )
        return when (channelPrivate) {
            is Success -> ResponseEntity
                .status(201)
                .body(
                    RegisterPrivateInviteOutputModel(
                        channelPrivate.value
                    )
                )

            is Failure -> when (channelPrivate.value) {
                InvitePrivateChannelError.UserAlreadyInChannel -> Problem.userAlreadyInChannel(
                    input.username,
                    instance
                )

                InvitePrivateChannelError.UserNotInChannel -> Problem.userNotInChannel(
                    authenticatedUser.user.username,
                    instance
                )

                InvitePrivateChannelError.UserPermissionsDeniedType -> Problem.userPermissionsDeniedType(
                    authenticatedUser.user.username,
                    instance
                )

                InvitePrivateChannelError.ChannelIsPublic -> Problem.channelIsPublic(id, instance)

                InvitePrivateChannelError.PrivacyTypeNotFound -> Problem.privacyTypeInvalid(
                    input.privacy.name,
                    instance
                )

                InvitePrivateChannelError.ChannelNotFound -> Problem.channelNotFound(id, instance)

                InvitePrivateChannelError.GuestNotFound -> Problem.usernameNotFound(input.username, instance)

                InvitePrivateChannelError.GuestIsBanned -> Problem.userIsBanned(input.username, id, instance)
            }
        }
    }


    @PostMapping(Uris.Channels.LEAVE_CHANNEL)
    fun leaveChannel(
        @PathVariable id: Int,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.leaveChannel(id)
        return when (val res = channelsService.leaveChannel(authenticatedUser.user.id, id)) {
            is Success -> ResponseEntity
                .status(200)
                .header(
                    "Location",
                    instance.toASCIIString()
                )
                .build<Unit>()

            is Failure -> when (res.value) {
                LeaveChannelResultError.UserNotInChannel -> Problem.userNotInChannel(
                    authenticatedUser.user.username,
                    instance
                )

                LeaveChannelResultError.ChannelNotFound -> Problem.channelNotFound(id, instance)
                LeaveChannelResultError.ErrorLeavingChannel -> Problem.errorLeavingChannel(id, instance)
            }
        }

    }


    @GetMapping(Uris.Channels.RECEIVED_CHANNEL_INVITES)
    fun getReceivedChannelInvites(
        authenticatedUser: AuthenticatedUser,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) offset: Int?,
    ): ResponseEntity<*> {
        val setLimit = limit ?: DEFAULT_LIMIT
        val setOffset = offset ?: DEFAULT_OFFSET
        val res = channelsService.getReceivedChannelInvites(authenticatedUser.user.id, setLimit, setOffset)
        return ResponseEntity
            .status(200)
            .body(PrivateInviteOutputModelList(res.map { elem ->
                PrivateInviteOutputModel(
                    elem.codHash,
                    elem.privacy,
                    elem.status,
                    elem.userInfo,
                    elem.channelId,
                    elem.channelName
                )
            }))
    }

    @GetMapping(Uris.Channels.SENT_CHANNEL_INVITES)
    fun getSentChannelInvites(
        authenticatedUser: AuthenticatedUser,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) offset: Int?,
    ): ResponseEntity<*> {
        val setLimit = limit ?: DEFAULT_LIMIT
        val setOffset = offset ?: DEFAULT_OFFSET
        val res = channelsService.getSentChannelInvites(authenticatedUser.user.id, setLimit, setOffset)
        return ResponseEntity
            .status(200)
            .body(PrivateInviteOutputModelList(res.map { elem ->
                PrivateInviteOutputModel(
                    elem.codHash,
                    elem.privacy,
                    elem.status,
                    elem.userInfo,
                    elem.channelId,
                    elem.channelName
                )
            }))
    }

    @PostMapping(Uris.Channels.BAN_USER)
    fun banUserFromChannel(
        authenticatedUser: AuthenticatedUser,
        @PathVariable id: Int,
        @RequestBody username: BanUserFromChannel
    ): ResponseEntity<*> {
        val instance = Uris.Channels.banUser(id)
        return when (
            val res = channelsService.banUserFromChannel(
                authenticatedUser.user.id,
                username.username,
                id
            )
        ) {
            is Success -> ResponseEntity
                .status(200)
                .body(
                    ChannelOutputModel(
                        res.value.id,
                        res.value.name,
                        res.value.owner,
                        res.value.type,
                        res.value.members,
                        res.value.bannedMembers
                    )
                )

            is Failure -> when (res.value) {
                BanUserResultError.UsernameNotFound -> Problem.usernameNotFound(username.username, instance)

                BanUserResultError.ChannelNotFound -> Problem.channelNotFound(id, instance)

                BanUserResultError.OwnerNotBanned -> Problem.ownerNotBanned(id, instance)

                BanUserResultError.UserAlreadyBanned -> Problem.userIsBanned(username.username, id, instance)

                BanUserResultError.UserIsNotOwner ->
                    Problem.userIsNotChannelOwner(
                        authenticatedUser.user.username,
                        instance
                    )

                BanUserResultError.UserNotInChannel -> Problem.userNotInChannel(username.username, instance)
            }
        }
    }

    @PostMapping(Uris.Channels.UNBAN_USER)
    fun unbanUserFromChannel(
        authenticatedUser: AuthenticatedUser,
        @PathVariable id: Int,
        @RequestBody username: BanUserFromChannel
    ): ResponseEntity<*> {
        val instance = Uris.Channels.unbanUser(id)
        return when (
            val res = channelsService.unbanUserFromChannel(
                authenticatedUser.user.id,
                username.username,
                id
            )
        ) {
            is Success -> ResponseEntity
                .status(200)
                .body(
                    ChannelOutputModel(
                        res.value.id,
                        res.value.name,
                        res.value.owner,
                        res.value.type,
                        res.value.members,
                        res.value.bannedMembers
                    )
                )

            is Failure -> when (res.value) {
                UnbanUserResultError.UsernameNotFound -> Problem.usernameNotFound(username.username, instance)

                UnbanUserResultError.ChannelNotFound -> Problem.channelNotFound(id, instance)

                UnbanUserResultError.OwnerNotBanned -> Problem.ownerNotBanned(id, instance)

                UnbanUserResultError.UserIsNotBanned -> Problem.userIsNotBanned(username.username, id, instance)

                UnbanUserResultError.UserIsNotOwner ->
                    Problem.userIsNotChannelOwner(
                        authenticatedUser.user.username,
                        instance
                    )
            }
        }
    }
}

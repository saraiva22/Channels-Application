package pt.isel.daw.channels.http.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
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
                        res.value.members
                    )
                )

            is Failure -> when (res.value) {
                GetChannelByIdError.ChannelNotFound -> Problem.channelNotFound(id, instance)
                GetChannelByIdError.PermissionDenied -> Problem.unauthorized(instance)
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
        val res = channelsService.getChannelByName(userId, name, sortParam)
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
        val sortParam = sort?.let { Sort.fromString(sort) }
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

    @PutMapping(Uris.Channels.UPDATE)
    fun updateNameChannel(
        @PathVariable id: Int,
        @RequestBody input: ChannelUpdateInputModel,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.update(id)
        val userId = authenticatedUser.user.id
        return when (val updateChannel = channelsService.updateNameChannel(input.name, id, userId)) {
            is Success ->
                ResponseEntity
                    .status(200)
                    .body(
                        ChannelOutputModel(
                            updateChannel.value.id,
                            updateChannel.value.name,
                            updateChannel.value.owner,
                            updateChannel.value.type,
                            updateChannel.value.members
                        )
                    )

            is Failure -> {
                when (updateChannel.value) {
                    UpdateNameChannelError.UserNotInChannel -> Problem.userNotInChannel(
                        authenticatedUser.user.username,
                        instance
                    )

                    UpdateNameChannelError.ChannelNameAlreadyExists -> Problem.channelNameAlreadyExists(
                        input.name,
                        instance
                    )

                    UpdateNameChannelError.ChannelNotFound -> Problem.channelNotFound(
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
        return when (val channel = channelsService.joinUsersInPublicChannel(userAuthenticatedUser.user.id, id)) {
            is Success -> ResponseEntity
                .status(200)
                .body(
                    ChannelOutputModel(
                        channel.value.id,
                        channel.value.name,
                        channel.value.owner,
                        channel.value.type,
                        channel.value.members
                    )
                )

            is Failure -> when (channel.value) {
                JoinUserInChannelPublicError.UserAlreadyInChannel -> Problem.userAlreadyInChannel(
                    userAuthenticatedUser.user.username,
                    instance
                )

                JoinUserInChannelPublicError.ChannelNotFound -> Problem.channelNotFound(id, instance)
                JoinUserInChannelPublicError.IsPrivateChannel -> Problem.isPrivateChannel(id, instance)
            }
        }

    }


    @PostMapping(Uris.Channels.JOIN_PRIVATE_CHANNELS)
    fun joinPrivateChannel(
        @PathVariable id: Int,
        @RequestBody input: RegisterPrivateInviteModel,
        userAuthenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.joinPrivateChannel(id)
        val channel = channelsService.joinUsersInPrivateChannel(userAuthenticatedUser.user.id, id, input.codHash)
        return when (channel) {
            is Success -> ResponseEntity
                .status(200)
                .body(
                    ChannelOutputModel(
                        channel.value.id,
                        channel.value.name,
                        channel.value.owner,
                        channel.value.type,
                        channel.value.members
                    )
                )

            is Failure -> when (channel.value) {
                JoinUserInChannelPrivateError.UserAlreadyInChannel -> Problem.userAlreadyInChannel(
                    userAuthenticatedUser.user.username,
                    instance
                )

                JoinUserInChannelPrivateError.CodeInvalidOrExpired -> Problem.codeInvalidOrExpiredChannel(
                    input.codHash,
                    instance
                )

                JoinUserInChannelPrivateError.ChannelNotFound -> Problem.channelNotFound(id, instance)
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
            id,
            authenticatedUser.user.id,
            input.username,
            input.privacy
        )
        return when (channelPrivate) {
            is Success -> ResponseEntity
                .status(201)
                .body(
                    RegisterPrivateInviteModel(
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

                InvitePrivateChannelError.UserNotPermissionsType -> Problem.userNotPermissionsType(
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
}
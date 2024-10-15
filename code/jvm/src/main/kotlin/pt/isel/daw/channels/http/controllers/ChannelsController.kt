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
import pt.isel.daw.channels.domain.user.AuthenticatedUser
import pt.isel.daw.channels.http.Uris
import pt.isel.daw.channels.http.model.Problem
import pt.isel.daw.channels.http.model.channel.*
import pt.isel.daw.channels.services.channel.*
import pt.isel.daw.channels.services.user.UserSearchError
import pt.isel.daw.channels.services.user.UsersService
import pt.isel.daw.channels.utils.Failure
import pt.isel.daw.channels.utils.Success

@RestController
class ChannelsController(
    private val channelsService: ChannelsService,
    private val usersService: UsersService
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
                .build<Unit>()

            is Failure -> when (res.value) {
                ChannelCreationError.ChannelAlreadyExists -> Problem.channelAlreadyExists(instance)
            }
        }
    }

    @GetMapping(Uris.Channels.GET_PUBLIC_CHANNELS)
    fun getPublicChannels(
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = channelsService.getPublicChannels()
        return ResponseEntity
            .status(200)
            .body(ChannelsListOutputModel(res))
    }

    @GetMapping(Uris.Channels.GET_BY_ID)
    fun getChannelById(
        @PathVariable id: Int,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.register()
        val userId = authenticatedUser.user.id
        return when (val res = channelsService.getAccessibleChannelById(userId, id)) {
            is Success -> ResponseEntity
                .status(200)
                .body(
                    ChannelOutputModel(
                        res.value.id,
                        res.value.name,
                        res.value.owner,
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
    fun getChannelByName(
        @RequestParam name: String,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.register()
        val userId = authenticatedUser.user.id
        return when (val res = channelsService.getChannelByName(userId, name)) {
            is Success -> ResponseEntity
                .status(200)
                .body(
                    ChannelOutputModel(
                        res.value.id,
                        res.value.name,
                        res.value.owner,
                        res.value.members
                    )
                )

            is Failure -> when (res.value) {
                GetChannelByNameError.ChannelNameNotFound -> Problem.channelNameNotFound(name, instance)
                GetChannelByNameError.PermissionDenied -> Problem.unauthorized(instance)
            }
        }
    }

    @GetMapping(Uris.Channels.GET_BY_USER)
    fun getUserChannels(
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val userId = authenticatedUser.user.id
        val res = channelsService.getUserChannels(userId)
        return ResponseEntity
            .status(200)
            .body(ChannelsListOutputModel(res))
    }

    @PutMapping(Uris.Channels.UPDATE)
    fun updateNameChannel(
        @PathVariable id: Int,
        @RequestBody input: ChannelUpdateOutputModel,
        authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.update(id)
        val userId = authenticatedUser.user.id
        val channel = channelsService.getAccessibleChannelById(userId, id)
        return when (channel) {
            is Success -> {
                val updateChannel =
                    channelsService.updateNameChannel(input.name, id, authenticatedUser.user.id)
                return when (updateChannel) {
                    is Success -> {
                        ResponseEntity
                            .status(200)
                            .body(
                                ChannelOutputModel(
                                    updateChannel.value.id,
                                    updateChannel.value.name,
                                    updateChannel.value.owner,
                                    updateChannel.value.members
                                )
                            )
                    }

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
                        }
                    }
                }
            }

            is Failure -> when (channel.value) {
                GetChannelByIdError.ChannelNotFound -> Problem.channelNotFound(id, instance)
                GetChannelByIdError.PermissionDenied -> Problem.unauthorized(instance)
            }
        }
    }

    @PutMapping(Uris.Channels.JOIN_PUBLIC_CHANNELS)
    fun joinPublicChannel(
        @PathVariable id: Int,
        userAuthenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.joinPublicChannel(id)
        val channel = channelsService.joinUsersInPublicChannel(userAuthenticatedUser.user.id, id)
        return when (channel) {
            is Success -> ResponseEntity
                .status(200)
                .body(
                    ChannelOutputModel(
                        channel.value.id,
                        channel.value.name,
                        channel.value.owner,
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


    @PutMapping(Uris.Channels.JOIN_PRIVATE_CHANNELS)
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
                        channel.value.members
                    )
                )

            is Failure -> when (channel.value) {
                JoinUserInChannelPrivateError.UserAlreadyInChannel -> Problem.userAlreadyInChannel(
                    userAuthenticatedUser.user.username,
                    instance
                )

                JoinUserInChannelPrivateError.CodeInvalid -> Problem.codeInvalidChannel(id, input.codHash, instance)
                JoinUserInChannelPrivateError.ChannelNotFound -> Problem.channelNotFound(id, instance)
            }
        }
    }

    @PutMapping(Uris.Channels.CREATE_PRIVATE_INVITE)
    fun invitePrivateChannel(
        @PathVariable id: Int,
        @RequestBody input: ChannelPrivateInviteInput,
        authenticatedUser: AuthenticatedUser,
    ): ResponseEntity<*> {
        val instance = Uris.Channels.invitePrivateChannel(id)
        val userId = authenticatedUser.user.id
        val channel = channelsService.getChannelById(id)
        return when (channel) {
            is Success -> {
                val guestUser = usersService.getUserByName(input.username)
                when (guestUser) {
                    is Success -> {
                        val channelPrivate = channelsService.invitePrivateChannel(
                            channel.value,
                            authenticatedUser.user.id,
                            guestUser.value,
                            input.privacy
                        )
                        when (channelPrivate) {
                            is Success -> ResponseEntity
                                .status(200)
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

                                InvitePrivateChannelError.ChannelIsPublic -> Problem.channelIsPublic(id,instance)

                                InvitePrivateChannelError.PrivacyTypeNotFound -> Problem.privacyTypeInvalid(input.privacy.name,instance)
                            }
                        }
                    }

                    is Failure -> when (guestUser.value) {
                        UserSearchError.UserNotFound -> Problem.usernameNotFound(input.username, instance)
                    }
                }
            }

            is Failure -> when (channel.value) {
                GetChannelSimpleByIdError.ChannelNotFound -> Problem.channelNotFound(id, instance)
            }
        }

    }
}
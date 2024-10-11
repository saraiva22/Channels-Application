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
import pt.isel.daw.channels.http.model.channel.ChannelCreateInputModel
import pt.isel.daw.channels.http.model.Problem
import pt.isel.daw.channels.http.model.channel.ChannelOutputModel
import pt.isel.daw.channels.http.model.channel.ChannelUpdateOutputModel
import pt.isel.daw.channels.http.model.channel.ChannelsListOutputModel
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
        //authenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.register()
        val channel = ChannelModel(input.name, input.owner, input.type)
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
    fun getPublicChannels(): ResponseEntity<*> {
        val res = channelsService.getPublicChannels()
        return ResponseEntity
            .status(200)
            .body(ChannelsListOutputModel(res))
    }

    @GetMapping(Uris.Channels.GET_BY_ID)
    fun getChannelById(
        @PathVariable id: Int
    ): ResponseEntity<*> {
        val instance = Uris.Channels.register()
        return when (val res = channelsService.getChannelById(id)) {
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
                GetChannelError.ChannelNotFound -> Problem.channelNotFound(id, instance)
            }
        }
    }

    @GetMapping(Uris.Channels.GET_BY_NAME)
    fun getChannelByName(
        @RequestParam name: String
    ): ResponseEntity<*> {
        val instance = Uris.Channels.register()
        return when (val res = channelsService.getChannelByName(name)) {
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
                GetChannelNameError.ChannelNameNotFound -> Problem.channelNameNotFound(name, instance)
            }
        }
    }

    @GetMapping(Uris.Channels.GET_BY_USER)
    fun getUserChannels(
        @PathVariable id: Int
    ): ResponseEntity<*> {
        val instance = Uris.Channels.byId(id)
        val res = channelsService.getUserChannels(id)
        return when (res) {
            is Success -> ResponseEntity
                .status(200)
                .body(ChannelsListOutputModel(res.value))

            is Failure -> when (res.value) {
                GetUserChannelsError.UserNotFound -> Problem.userNotFound(id, instance)
            }
        }
    }

    @PutMapping(Uris.Channels.UPDATE)
    fun updateNameChannel(
        @PathVariable id: Int,
        @RequestBody input: ChannelUpdateOutputModel,
        userAuthenticatedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val instance = Uris.Channels.update(id)
        val channel = channelsService.getChannelById(id)
        return when (channel) {
            is Success -> {
                val updateChannel =
                    channelsService.updateNameChannel(input.name, id, userAuthenticatedUser.user.id)
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
                                userAuthenticatedUser.user.username,
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
                GetChannelError.ChannelNotFound -> Problem.channelNotFound(id, instance)
            }
        }
    }
}
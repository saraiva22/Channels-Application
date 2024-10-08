package pt.isel.daw.channels.http.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.http.Uris
import pt.isel.daw.channels.http.model.channel.ChannelCreateInputModel
import pt.isel.daw.channels.http.model.Problem
import pt.isel.daw.channels.http.model.channel.ChannelsListOutputModel
import pt.isel.daw.channels.services.channel.ChannelCreationError
import pt.isel.daw.channels.services.channel.ChannelsService
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
                ChannelCreationError.ChannelAlreadyExists -> Problem.response(400, Problem.channelAlreadyExists)
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

    companion object {
        val logger = LoggerFactory.getLogger(ChannelsController::class.java)
    }
}
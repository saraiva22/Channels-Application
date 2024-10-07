package pt.isel.daw.channels.http.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.http.Uris
import pt.isel.daw.channels.http.model.channel.ChannelCreateInputModel
import pt.isel.daw.channels.http.model.Problem
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
        @RequestBody input: ChannelCreateInputModel
    ): ResponseEntity<*> {
        val channel = ChannelModel(input.name, input.owner, input.rules, input.type)
        return when (val res = channelsService.createChannel(channel)) {
            is Success -> ResponseEntity
                .status(201)
                .header(
                    "Location",
                    // improve in order to get the channel
                    res.value.toString()
                )
                .build<Unit>()

            is Failure -> when (res.value) {
                ChannelCreationError.ChannelAlreadyExists -> Problem.response(400, Problem.channelAlreadyExists)
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(ChannelsController::class.java)
    }
}
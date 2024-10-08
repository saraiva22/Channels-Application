package pt.isel.daw.channels.services.channel

import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.utils.Either

sealed class ChannelCreationError {
    data object ChannelAlreadyExists: ChannelCreationError()
}

typealias ChannelCreationResult = Either<ChannelCreationError, Int>

sealed class GetChannelError {
    data object ChannelDoesNotExists: GetChannelError()
}

typealias GetChannelResult = Either<GetChannelError, Channel>

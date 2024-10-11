package pt.isel.daw.channels.services.channel

import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.utils.Either

sealed class ChannelCreationError {
    data object ChannelAlreadyExists : ChannelCreationError()
}

typealias ChannelCreationResult = Either<ChannelCreationError, Int>

sealed class GetChannelError {
    data object ChannelNotFound : GetChannelError()
}

typealias GetChannelResult = Either<GetChannelError, Channel>


sealed class GetChannelNameError {
    data object ChannelNameNotFound : GetChannelNameError()
}

typealias GetChannelNameResult = Either<GetChannelNameError, Channel>


sealed class GetUserChannelsError {
    data object UserNotFound : GetUserChannelsError()
}

typealias GetUserChannelsResult = Either<GetUserChannelsError, List<Channel>>

sealed class UpdateNameChannelError {
    data object UserNotInChannel : UpdateNameChannelError()
    data object ChannelNameAlreadyExists : UpdateNameChannelError()
}

typealias UpdateNameChannelResult = Either<UpdateNameChannelError, Channel>

sealed class JoinUserInChannelError{
    data object UserAlreadyInChannel : JoinUserInChannelError()
}

typealias GetUserChannelResult = Either<JoinUserInChannelError, Channel>
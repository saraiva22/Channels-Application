package pt.isel.daw.channels.services.channel

import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.utils.Either

sealed class ChannelCreationError {
    data object ChannelAlreadyExists : ChannelCreationError()
}

typealias ChannelCreationResult = Either<ChannelCreationError, Int>

sealed class GetChannelByIdError {
    data object ChannelNotFound : GetChannelByIdError()
}

typealias GetChannelResult = Either<GetChannelByIdError, Channel>

sealed class GetChannelByNameError {
    data object ChannelNameNotFound : GetChannelByNameError()
}

typealias GetChannelByNameResult = Either<GetChannelByNameError, Channel>

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
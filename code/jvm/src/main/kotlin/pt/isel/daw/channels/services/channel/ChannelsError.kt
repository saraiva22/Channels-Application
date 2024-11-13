package pt.isel.daw.channels.services.channel

import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.utils.Either

sealed class ChannelCreationError {
    data object ChannelAlreadyExists : ChannelCreationError()
}

typealias ChannelCreationResult = Either<ChannelCreationError, Int>

sealed class GetChannelByIdError {
    data object ChannelNotFound : GetChannelByIdError()
    data object PermissionDenied: GetChannelByIdError()
}

typealias GetChannelResult = Either<GetChannelByIdError, Channel>

sealed class GetChannelSimpleByIdError {
    data object ChannelNotFound : GetChannelSimpleByIdError()
}

typealias GetChannelSimpleResult = Either<GetChannelSimpleByIdError, Channel>


sealed class GetChannelByNameError {
    data object ChannelNameNotFound : GetChannelByNameError()
    data object PermissionDenied: GetChannelByNameError()
}

typealias GetChannelByNameResult = Either<GetChannelByNameError, Channel>

sealed class GetUserChannelsError {
    data object UserNotFound : GetUserChannelsError()
}

typealias GetUserChannelsResult = Either<GetUserChannelsError, List<Channel>>

sealed class UpdateNameChannelError {
    data object UserNotInChannel : UpdateNameChannelError()
    data object ChannelNameAlreadyExists : UpdateNameChannelError()
    data object ChannelNotFound : UpdateNameChannelError()
}

typealias UpdateNameChannelResult = Either<UpdateNameChannelError, Channel>

sealed class JoinUserInChannelPublicError{
    data object UserAlreadyInChannel : JoinUserInChannelPublicError()
    data object ChannelNotFound : JoinUserInChannelPublicError()
    data object ChannelIsPrivate : JoinUserInChannelPublicError()
}

typealias JoinUserInChannelPublicResult = Either<JoinUserInChannelPublicError, Channel>


sealed class JoinUserInChannelPrivateError{
    data object UserAlreadyInChannel : JoinUserInChannelPrivateError()
    data object InvalidCode : JoinUserInChannelPrivateError()
    data object ChannelNotFound : JoinUserInChannelPrivateError()
    data object InviteRejected : JoinUserInChannelPrivateError()
}

typealias JoinUserInChannelPrivateResult = Either<JoinUserInChannelPrivateError, Channel>

sealed class InvitePrivateChannelError{
    data object UserAlreadyInChannel : InvitePrivateChannelError()
    data object UserNotInChannel : InvitePrivateChannelError()
    data object UserPermissionsDeniedType : InvitePrivateChannelError()
    data object ChannelIsPublic : InvitePrivateChannelError()
    data object PrivacyTypeNotFound : InvitePrivateChannelError()
    data object ChannelNotFound : InvitePrivateChannelError()
    data object GuestNotFound : InvitePrivateChannelError()
}

typealias InvitePrivateChannelResult = Either<InvitePrivateChannelError, String>


sealed class LeaveChannelResultError{
    data object UserNotInChannel : LeaveChannelResultError()
    data object ChannelNotFound : LeaveChannelResultError()
    data object ErrorLeavingChannel : LeaveChannelResultError()
}

typealias LeaveChannelResult = Either<LeaveChannelResultError, Unit>
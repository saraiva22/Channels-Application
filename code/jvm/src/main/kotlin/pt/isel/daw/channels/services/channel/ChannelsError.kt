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

sealed class UpdateChannelError {
    data object UserNotInChannel : UpdateChannelError()
    data object UserNotChannelOwner : UpdateChannelError()
    data object ChannelNameAlreadyExists : UpdateChannelError()
    data object ChannelNotFound : UpdateChannelError()
}

typealias UpdateChannelResult = Either<UpdateChannelError, Channel>

sealed class JoinUserInChannelPublicError{
    data object UserAlreadyInChannel : JoinUserInChannelPublicError()
    data object ChannelNotFound : JoinUserInChannelPublicError()
    data object ChannelIsPrivate : JoinUserInChannelPublicError()
    data object UserIsBanned : JoinUserInChannelPublicError()
}

typealias JoinUserInChannelPublicResult = Either<JoinUserInChannelPublicError, Channel>


sealed class ValidateChannelInviteError{
    data object UserAlreadyInChannel : ValidateChannelInviteError()
    data object InvalidCode : ValidateChannelInviteError()
    data object ChannelNotFound : ValidateChannelInviteError()
    data object InviteRejected : ValidateChannelInviteError()
    data object GuestIsBanned : ValidateChannelInviteError()
}

typealias ValidateChannelInviteResult = Either<ValidateChannelInviteError, Channel>

sealed class InvitePrivateChannelError{
    data object UserAlreadyInChannel : InvitePrivateChannelError()
    data object UserNotInChannel : InvitePrivateChannelError()
    data object UserPermissionsDeniedType : InvitePrivateChannelError()
    data object ChannelIsPublic : InvitePrivateChannelError()
    data object PrivacyTypeNotFound : InvitePrivateChannelError()
    data object ChannelNotFound : InvitePrivateChannelError()
    data object GuestNotFound : InvitePrivateChannelError()
    data object GuestIsBanned : InvitePrivateChannelError()
}

typealias InvitePrivateChannelResult = Either<InvitePrivateChannelError, String>


sealed class LeaveChannelResultError{
    data object UserNotInChannel : LeaveChannelResultError()
    data object ChannelNotFound : LeaveChannelResultError()
    data object ErrorLeavingChannel : LeaveChannelResultError()
}

typealias LeaveChannelResult = Either<LeaveChannelResultError, Unit>

sealed class BanUserResultError {
    data object UsernameNotFound : BanUserResultError()
    data object UserAlreadyBanned : BanUserResultError()
    data object UserNotInChannel : BanUserResultError()
    data object UserIsNotOwner : BanUserResultError()
    data object ChannelNotFound : BanUserResultError()
    data object OwnerNotBanned : BanUserResultError()
}

typealias BanUserResult = Either<BanUserResultError, Channel>

sealed class UnbanUserResultError {
    data object UsernameNotFound : UnbanUserResultError()
    data object UserIsNotBanned : UnbanUserResultError()
    data object UserIsNotOwner : UnbanUserResultError()
    data object ChannelNotFound : UnbanUserResultError()
    data object OwnerNotBanned : UnbanUserResultError()
}

typealias UnbanUserResult = Either<UnbanUserResultError, Channel>
package pt.isel.daw.channels.services.message

import pt.isel.daw.channels.domain.messages.Message
import pt.isel.daw.channels.utils.Either

sealed class GetMessageError {
    data object ChannelNotFound : GetMessageError()
    data object PermissionDenied : GetMessageError()
}

typealias GetMessageResult = Either<GetMessageError, List<Message>>

sealed class CreateMessageError {
    data object ChannelNotFound : CreateMessageError()
    data object UserNotMemberInChannel : CreateMessageError()
    data object PrivacyIsNotReadWrite : CreateMessageError()
}

typealias CreateMessageResult = Either<CreateMessageError, Int>
package pt.isel.daw.channels.services.message

import pt.isel.daw.channels.domain.messages.Message
import pt.isel.daw.channels.utils.Either

sealed class GetMessagesError {
    data object ChannelNotFound : GetMessagesError()
    data object PermissionDenied : GetMessagesError()
}

typealias GetMessagesResult = Either<GetMessagesError, List<Message>>


sealed class GetMessageError {
    data object ChannelNotFound : GetMessageError()
    data object PermissionDenied : GetMessageError()
    data object MessageNotFound : GetMessageError()
}

typealias GetMessageResult = Either<GetMessageError, Message>

sealed class CreateMessageError {
    data object ChannelNotFound : CreateMessageError()
    data object UserNotMemberInChannel : CreateMessageError()
    data object PrivacyIsNotReadWrite : CreateMessageError()
}

typealias CreateMessageResult = Either<CreateMessageError, Int>

sealed class DeleteMessageError {
    data object ChannelNotFound : DeleteMessageError()
    data object PermissionDenied : DeleteMessageError()
    data object MessageNotFound : DeleteMessageError()
}

typealias DeleteMessageResult = Either<DeleteMessageError, Boolean>
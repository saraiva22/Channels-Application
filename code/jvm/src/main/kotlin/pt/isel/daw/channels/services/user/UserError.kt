package pt.isel.daw.channels.services.user

import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.utils.Either


sealed class UserCreationError {
    data object UserNameAlreadyExists : UserCreationError()
    data object InsecurePassword : UserCreationError()
    data object EmailAlreadyExists : UserCreationError()
}

typealias UserCreationResult = Either<UserCreationError, Int>


sealed class UserSearchError {
    data object UserNotFound : UserSearchError()
}

typealias UserSearchResult = Either<UserSearchError, User>
package pt.isel.daw.channels.services.user

import kotlinx.datetime.Instant
import pt.isel.daw.channels.utils.Either

data class TokenExternalInfo(
    val tokenValue: String,
    val tokenExpiration: Instant
)

sealed class TokenCreationError {
    data object UserOrPasswordAreInvalid : TokenCreationError()
    data object InvalidToken : TokenCreationError()
}

typealias TokenCreationResult = Either<TokenCreationError, TokenExternalInfo>


sealed class TokenRevocationError {
    data object TokenIsInvalid : TokenRevocationError()
}

typealias TokenRevocationResult = Either<TokenRevocationError, Boolean>



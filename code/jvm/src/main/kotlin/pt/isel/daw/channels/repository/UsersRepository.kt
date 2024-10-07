package pt.isel.daw.channels.repository

import kotlinx.datetime.Instant
import pt.isel.daw.channels.domain.user.PasswordValidationInfo
import pt.isel.daw.channels.domain.user.Token
import pt.isel.daw.channels.domain.user.TokenValidationInfo
import pt.isel.daw.channels.domain.user.User

interface UsersRepository {
    fun storeUser(
        username: String,
        email: String,
        passwordValidation: PasswordValidationInfo
    ):Int

    fun getUserByUsername(username: String): User?

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    fun isUserStoredByUsername(username: String): Boolean

    fun createToken(token: Token, maxTokens: Int)

    fun updateTokenLastUsed(token: Token, now: Instant)

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int


}
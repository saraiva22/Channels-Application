package pt.isel.daw.channels.repository

import kotlinx.datetime.Instant
import pt.isel.daw.channels.domain.user.PasswordValidationInfo
import pt.isel.daw.channels.domain.token.Token
import pt.isel.daw.channels.domain.token.TokenValidationInfo
import pt.isel.daw.channels.http.model.user.RegisterModel
import pt.isel.daw.channels.domain.user.User

interface UsersRepository {
    fun storeUser(
        username: String,
        email: String,
        passwordValidation: PasswordValidationInfo,
    ): Int

    fun getUserByUsername(username: String): User?

    fun getUserById(id: Int): User?

    fun getUserByEmail(email: String): User?

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    fun isUserStoredByUsername(username: String): Boolean

    fun isEmailStoredByEmail(email: String): Boolean

    fun createToken(token: Token, maxTokens: Int)

    fun updateTokenLastUsed(token: Token, now: Instant)

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int

    fun createRegisterInvite(register: RegisterModel)

    fun isInviteCodeInvalid(inviteCode: String): Boolean

    fun invalidateCode(inviteCode: String)
}
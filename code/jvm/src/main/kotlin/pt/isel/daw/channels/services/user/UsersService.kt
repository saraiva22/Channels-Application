package pt.isel.daw.channels.services.user

import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pt.isel.daw.channels.domain.token.Token
import pt.isel.daw.channels.http.model.user.RegisterModel
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.domain.user.UsersDomain
import pt.isel.daw.channels.repository.TransactionManager
import pt.isel.daw.channels.utils.Either
import pt.isel.daw.channels.utils.failure
import pt.isel.daw.channels.utils.success


@Component
class UsersService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val clock: Clock
) {
    fun createUser(
        username: String,
        email: String,
        password: String,
        inviteCode: String?
    ): UserCreationResult {
        if (!usersDomain.isSafePassword(password)) {
            return failure(UserCreationError.InsecurePassword)
        }
        val passwordValidationInfo = usersDomain.createPasswordValidationInformation(password)

        return transactionManager.run {
            val usersRepository = it.usersRepository
            if (!usersRepository.hasUsers()) {
                val id = usersRepository.storeUser(username, email, passwordValidationInfo)
                return@run success(id)
            }
            if (inviteCode == null) {
                return@run failure(UserCreationError.InvalidInviteCode)
            }
            val codeValidation = usersRepository.codeValidation(inviteCode)

            when {
                usersRepository.isUserStoredByUsername(username) ->
                    return@run failure(UserCreationError.UserNameAlreadyExists)

                usersRepository.isEmailStoredByEmail(email) ->
                    return@run failure(UserCreationError.EmailAlreadyExists)

                codeValidation == null || codeValidation.expired ->
                    return@run failure(UserCreationError.InvalidInviteCode)
            }

            val id = usersRepository.storeUser(username, email, passwordValidationInfo)
            usersRepository.invalidateCode(inviteCode)
            success(id)
        }
    }


    fun createToken(username: String, password: String): TokenCreationResult {
        if (username.isBlank() || password.isBlank()) {
            failure(TokenCreationError.UserOrPasswordAreInvalid)
        }
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val user: User = usersRepository.getUserByUsername(username)
                ?: return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
            if (!usersDomain.validatePassword(password, user.passwordValidation)) {
                return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
            }
            val tokenValue = usersDomain.generateTokenValue()
            val now = clock.now()
            val newToken = Token(
                usersDomain.createTokenValidationInformation(tokenValue),
                user.id,
                createdAt = now,
                lastUsedAt = now,
            )
            usersRepository.createToken(newToken, usersDomain.maxNumberOfTokensPerUser)
            Either.Right(
                TokenExternalInfo(
                    tokenValue,
                    usersDomain.getTokenExpiration(newToken),
                ),
            )
        }
    }


    fun getUserById(id: Int): UserSearchResult = transactionManager.run {
        val usersRepository = it.usersRepository
        val user = usersRepository.getUserById(id) ?: return@run failure(UserSearchError.UserNotFound)
        success(user)
    }

    fun getUserByName(username: String): UserSearchResult = transactionManager.run {
        val usersRepository = it.usersRepository
        val user = usersRepository.getUserByUsername(username) ?: return@run failure(UserSearchError.UserNotFound)
        success(user)
    }


    fun getUserByToken(token: String): User? {
        if (!usersDomain.canBeToken(token)) return null

        return transactionManager.run {
            val usersRepository = it.usersRepository
            val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
            val userAndToken = usersRepository.getTokenByTokenValidationInfo(tokenValidationInfo)
            if (userAndToken != null && usersDomain.isTokenTimeValid(clock, userAndToken.second)) {
                usersRepository.updateTokenLastUsed(userAndToken.second, clock.now())
                userAndToken.first
            } else {
                null
            }
        }
    }


    fun revokeToken(token: String): Boolean {
        val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
        return transactionManager.run {
            it.usersRepository.removeTokenByValidationInfo(tokenValidationInfo)
            logger.info("Token Revoked")
            true
        }
    }

    fun createRegisterInvite(userId: Int): String {
        val codHash = usersDomain.generateInvitation()
        val register = RegisterModel(userId, codHash, false)
        return transactionManager.run {
            it.usersRepository.createRegisterInvite(register)
            codHash
        }
    }

    fun getRandomUser(): User? {
        return transactionManager.run {
            it.usersRepository.getRandomUser()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UsersService::class.java)
    }


}
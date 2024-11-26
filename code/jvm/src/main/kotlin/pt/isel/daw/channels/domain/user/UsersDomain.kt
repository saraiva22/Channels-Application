package pt.isel.daw.channels.domain.user

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pt.isel.daw.channels.domain.token.Token
import pt.isel.daw.channels.domain.token.TokenEncoder
import pt.isel.daw.channels.domain.token.TokenValidationInfo
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

@Component
class UsersDomain(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UsersDomainConfig,
) {


    fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

    fun canBeToken(token: String): Boolean = try {
        Base64.getUrlDecoder()
            .decode(token).size == config.tokenSizeInBytes
    } catch (ex: IllegalArgumentException) {
        false
    }

    fun validatePassword(password: String, validationInfo: PasswordValidationInfo) = passwordEncoder.matches(
        password,
        validationInfo.validationInfo,
    )

    fun createPasswordValidationInformation(password: String) = PasswordValidationInfo(
        validationInfo = passwordEncoder.encode(password),
    )

    fun isTokenTimeValid(
        clock: Clock,
        token: Token,
    ): Boolean {
        val now = clock.now()
        return token.createdAt <= now &&
                (now - token.createdAt) <= config.tokenTtl &&
                (now - token.lastUsedAt) <= config.tokenRollingTtl
    }

    fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }

    fun createTokenValidationInformation(token: String): TokenValidationInfo =
        tokenEncoder.createValidationInformation(token)


    /**
     * Checks if the password is safe.
     * A safe password must have at least one lowercase letter, one uppercase letter, one digit, one special character
     * and must have a length between [MIN_PASSWORD_SIZE] and [MAX_PASSWORD_SIZE].
     */
    fun isSafePassword(password: String): Boolean {
        val regex =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\$@\$!%*?_&#])[A-Za-z\\d\$@\$!%*?_&#]{$MIN_PASSWORD_SIZE,$MAX_PASSWORD_SIZE}\$"
        return password.matches(regex.toRegex())
    }

    val maxNumberOfTokensPerUser = config.maxTokensPerUser

    fun isValidInvite(invite: String): Boolean {
        val hashedInvite = hashInvite(invite)
        return checkInvite(invite, hashedInvite)
    }

    fun hashInvite(invite: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(invite.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }


    fun generateInvitation(): String {
        val part1 = UUID.randomUUID().toString().take(5)
        val part2 = UUID.randomUUID().toString().take(5)
        return "$part1-$part2"
    }

    private fun checkInvite(invite: String, hash: String) = hashInvite(invite) == hash

    companion object {
        private const val MIN_PASSWORD_SIZE = 5
        private const val MAX_PASSWORD_SIZE = 32
    }
}
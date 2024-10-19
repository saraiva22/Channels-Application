package pt.isel.daw.channels.services

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import pt.isel.daw.channels.TestClock
import pt.isel.daw.channels.utils.Either
import java.util.*
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.fail
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class UsersServiceTests: ServiceTests() {

    /*
    @Test
    fun `create a user`()
    // include getUserByUsername, getUserByEmail, isEmailStoredByEmail, isUsernameStoredByUsername
     */

    @Test
    fun `create user token`() {
        // given: a user service
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // then: creating a user
        val userName = newTestUserName()
        val email = newTestEmail(userName)
        val passwordValidationInfo = newTokenValidationData()
        val code = generateInvitationCode()
        val createUserResult = createUserInService(userName, email, passwordValidationInfo, code)

        // when: creating a token
        val createTokenResult = userService.createToken(createUserResult.username, passwordValidationInfo)

        // then: the creation is successful
        val token = when (createTokenResult) {
            is Either.Left -> fail(createTokenResult.toString())
            is Either.Right -> createTokenResult.value.tokenValue
        }

        // when: retrieving the user by token
        val user = userService.getUserByToken(token)

        // then: the user is found
        assertNotNull(user)
        assertEquals(createUserResult.username, user.username)
        assertEquals(createUserResult.email, user.email)
        assertEquals(createUserResult.passwordValidation, user.passwordValidation)

        // finally: clear the data
        clearData(jdbi, "dbo.Tokens", "user_id", user.id)
        clearData(jdbi, "dbo.Users", "id", user.id)
    }

    @Test
    fun `can use token during rolling period but not after absolute TTL`() {
        // given: a user service
        val testClock = TestClock()
        val tokenTtl = 90.minutes
        val tokenRollingTtl = 30.minutes
        val userService = createUsersService(testClock, tokenTtl, tokenRollingTtl)

        // then: creating a user
        val userName = newTestUserName()
        val email = newTestEmail(userName)
        val passwordValidationInfo = newTokenValidationData()
        val code = generateInvitationCode()
        val createUserResult = createUserInService(userName, email, passwordValidationInfo, code)

        // when: creating a token
        val createTokenResult = userService.createToken(createUserResult.username, passwordValidationInfo)

        // then: the creation is successful
        val token = when (createTokenResult) {
            is Either.Left -> fail(createTokenResult.toString())
            is Either.Right -> createTokenResult.value.tokenValue
        }

        // when: retrieving the user after (rolling TTL - 1s) intervals
        val startInstant = testClock.now()
        while (true) {
            testClock.advance(tokenRollingTtl.minus(1.seconds))
            userService.getUserByToken(token) ?: break
        }

        // then: user is not found only after the absolute TTL has elapsed
        assertTrue((testClock.now() - startInstant) > tokenTtl)

        // finally: clear the data
        clearData(jdbi, "dbo.Tokens", "user_id", createUserResult.id)
        clearData(jdbi, "dbo.Users", "id", createUserResult.id)
    }

    @Test
    fun `revoke user token`() {
        // given: a user service
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // then: creating a user
        val userName = newTestUserName()
        val email = newTestEmail(userName)
        val passwordValidationInfo = newTokenValidationData()
        val code = generateInvitationCode()
        val createUserResult = createUserInService(userName, email, passwordValidationInfo, code)

        // when: creating a token
        val createTokenResult = userService.createToken(createUserResult.username, passwordValidationInfo)

        // then: the creation is successful
        val token = when (createTokenResult) {
            is Either.Left -> fail(createTokenResult.toString())
            is Either.Right -> createTokenResult.value.tokenValue
        }

        // when: revoking the token
        userService.revokeToken(token)

        // then: the token is no longer valid
        val user = userService.getUserByToken(token)
        assertNull(user)

        // finally: clear the data
        clearData(jdbi, "dbo.Tokens", "user_id", createUserResult.id)
        clearData(jdbi, "dbo.Users", "id", createUserResult.id)
    }

    @Test
    fun `create register invite`() {
        // given: a user service
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // then: creating a user
        val userName = newTestUserName()
        val email = newTestEmail(userName)
        val passwordValidationInfo = newTokenValidationData()
        val code = generateInvitationCode()
        val createUserResult = createUserInService(userName, email, passwordValidationInfo, code)

        // when: creating a register invite
        val inviteCode = userService.createRegisterInvite(createUserResult.id)

        // then: the creation is successful
        assertNotNull(inviteCode)

        // finally: clear the data
        clearData(jdbi, "dbo.Invitation_Register", "user_id", createUserResult.id)
        clearData(jdbi, "dbo.Users", "id", createUserResult.id)
    }

    companion object {
        private fun clearData(jdbi: Jdbi, tableName: String, columnName: String, userId: Int) {
            jdbi.useHandle<Exception> { handle ->
                handle.execute("delete from $tableName where $columnName = $userId")
            }
        }

        private fun generateInvitation(): String {
            val part1 = UUID.randomUUID().toString().take(4)
            val part2 = UUID.randomUUID().toString().take(4)
            return "$part1-$part2"
        }
    }
}
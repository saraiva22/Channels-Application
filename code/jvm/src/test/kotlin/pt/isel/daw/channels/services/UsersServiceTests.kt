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

/*
class UsersServiceTests: ServiceTests() {

    @Test
    fun `can create user, token, and retrieve by token`(){
        /// given: a user service
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // when: creating a user
        val username = newTestUserName()
        val password = "changeit"
        val email = newTestEmail(username)
        val invite = generateInvitationCode()
        val createUserResult = userService.createUser(username, email, password, invite)

        // then: the creation is successful
        when (createUserResult) {
            is Either.Left -> fail("Unexpected $createUserResult")
            is Either.Right -> assertTrue(createUserResult.value > 0)
        }

        // when: creating a token
        val createTokenResult = userService.createToken(username, password)

        // then: the creation is successful
        val token = when (createTokenResult) {
            is Either.Left -> fail(createTokenResult.toString())
            is Either.Right -> createTokenResult.value.tokenValue
        }

        // and: the token bytes have the expected length
        val tokenBytes = Base64.getUrlDecoder().decode(token)
        kotlin.test.assertEquals(256 / 8, tokenBytes.size)

        // when: retrieving the user by token
        val user = userService.getUserByToken(token)

        // then: a user is found
        assertNotNull(user)

        // and: has the expected name
        kotlin.test.assertEquals(username, user.username)

    }


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

    @Test
    fun `can limit the number of tokens`() {
        // given: a user service
        val testClock = TestClock()
        val maxTokensPerUser = 5
        val userService = createUsersService(testClock, maxTokensPerUser = maxTokensPerUser)

        // when: creating a user
        val username = newTestUserName()
        val password = "changeit"
        val email = newTestEmail(username)
        val invite = generateInvitationCode()
        val createUserResult = userService.createUser(username,email,  password, invite)

        // then: the creation is successful
        when (createUserResult) {
            is Either.Left -> fail("Unexpected $createUserResult")
            is Either.Right -> kotlin.test.assertTrue(createUserResult.value > 0)
        }

        // when: creating MAX tokens
        val tokens = (0 until maxTokensPerUser).map {
            val createTokenResult = userService.createToken(username, password)
            testClock.advance(1.minutes)

            // then: the creation is successful
            val token = when (createTokenResult) {
                is Either.Left -> fail(createTokenResult.toString())
                is Either.Right -> createTokenResult.value
            }
            token
        }.toTypedArray().reversedArray()

        // and: using the tokens at different times
        (tokens.indices).forEach {
            assertNotNull(userService.getUserByToken(tokens[it].tokenValue), "token $it must be valid")
            testClock.advance(1.seconds)
        }

        // and: creating a new token
        val createTokenResult = userService.createToken(username, password)
        testClock.advance(1.seconds)
        val newToken = when (createTokenResult) {
            is Either.Left -> fail(createTokenResult.toString())
            is Either.Right -> createTokenResult.value
        }

        // then: newToken is valid
        assertNotNull(userService.getUserByToken(newToken.tokenValue))

        // and: the first token (the least recently used) is not valid
        assertNull(userService.getUserByToken(tokens[0].tokenValue))

        // and: the remaining tokens are still valid
        (1 until tokens.size).forEach {
            assertNotNull(userService.getUserByToken(tokens[it].tokenValue))
        }
    }

    @Test
    fun `can limit the number of tokens even if multiple tokens are used at the same time`() {
        // given: a user service
        val testClock = TestClock()
        val maxTokensPerUser = 5
        val userService = createUsersService(testClock, maxTokensPerUser = maxTokensPerUser)

        // when: creating a user
        val username = newTestUserName()
        val password = "changeit"
        val email = newTestEmail(username)
        val invite = generateInvitationCode()
        val createUserResult = userService.createUser(username,email,  password, invite)

        // then: the creation is successful
        when (createUserResult) {
            is Either.Left -> fail("Unexpected $createUserResult")
            is Either.Right -> kotlin.test.assertTrue(createUserResult.value > 0)
        }

        // when: creating MAX tokens
        val tokens = (0 until maxTokensPerUser).map {
            val createTokenResult = userService.createToken(username, password)
            testClock.advance(1.minutes)

            // then: the creation is successful
            val token = when (createTokenResult) {
                is Either.Left -> fail(createTokenResult.toString())
                is Either.Right -> createTokenResult.value
            }
            token
        }.toTypedArray().reversedArray()

        // and: using the tokens at the same time
        testClock.advance(1.minutes)
        (tokens.indices).forEach {
            assertNotNull(userService.getUserByToken(tokens[it].tokenValue), "token $it must be valid")
        }

        // and: creating a new token
        val createTokenResult = userService.createToken(username, password)
        testClock.advance(1.minutes)
        val newToken = when (createTokenResult) {
            is Either.Left -> fail(createTokenResult.toString())
            is Either.Right -> createTokenResult.value
        }

        // then: newToken is valid
        assertNotNull(userService.getUserByToken(newToken.tokenValue))

        // and: exactly one of the previous tokens is now not valid
        kotlin.test.assertEquals(
            maxTokensPerUser - 1,
            tokens.count {
                userService.getUserByToken(it.tokenValue) != null
            },
        )
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

 */
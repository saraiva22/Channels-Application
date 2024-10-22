package pt.isel.daw.channels.repository.jdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pt.isel.daw.channels.ApplicationTests
import pt.isel.daw.channels.TestClock
import pt.isel.daw.channels.domain.token.Token
import pt.isel.daw.channels.domain.token.TokenValidationInfo
import pt.isel.daw.channels.domain.user.PasswordValidationInfo
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.http.model.user.RegisterModel
import pt.isel.daw.channels.runWithHandle
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class JdbiUserRepositoryTests : ApplicationTests() {


    @Test
    fun `can create and retrieve user`() =
        runWithHandle { handle ->

            // given: a UserRepository
            val repo = JdbiUsersRepository(handle)

            //when: storing a user
            val userName = newTestUserName()
            val email = newTestEmail(userName)
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            repo.storeUser(userName, email, passwordValidationInfo)

            // and: retrieving a user
            val retrievedUser: User? = repo.getUserByUsername(userName)

            // then:
            assertNotNull(retrievedUser)
            assertEquals(userName, retrievedUser.username)
            assertEquals(passwordValidationInfo, retrievedUser.passwordValidation)
            assertTrue(retrievedUser.id >= 0)

            // when: asking if the user exists
            val isUserIsStored = repo.isUserStoredByUsername(userName)

            // then: response is true
            assertTrue(isUserIsStored)

            // when: asking if a different user exists
            val anotherUserIsStored = repo.isUserStoredByUsername("another-$userName")

            // then: response is false
            assertFalse(anotherUserIsStored)


        }


    // include getUserByUsername, getUserByEmail, isEmailStoredByEmail, isUsernameStoredByUsername


    @Test
    fun `create and retrieve token with associated user`() =
        runWithHandle(jdbi) { handle ->
            // given: a UsersRepository
            val repo = JdbiUsersRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: a user is created
            val userName = newTestUserName()
            val email = newTestEmail(userName)
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            repo.storeUser(userName, email, passwordValidationInfo)

            // then: the user is created
            val user = repo.getUserByUsername(userName)
            assertNotNull(user)

            // and: test TokenValidationInfo
            val testTokenValidationInfo = TokenValidationInfo(newTokenValidationData())

            // when: creating a token
            val tokenCreationInstant = clock.now()
            val token = Token(
                testTokenValidationInfo,
                user.id,
                createdAt = tokenCreationInstant,
                lastUsedAt = tokenCreationInstant,
            )
            repo.createToken(token, 1)

            // then: createToken does not throw errors
            // no exception

            // when: retrieving the token and associated user
            val userAndToken = repo.getTokenByTokenValidationInfo(testTokenValidationInfo)

            // then:
            val (associatedUser, retrievedToken) = userAndToken ?: fail("token and associated user must exist")

            // and: ...
            assertEquals(userName, associatedUser.username)
            assertEquals(testTokenValidationInfo.validationInfo, retrievedToken.tokenValidationInfo.validationInfo)
            assertEquals(tokenCreationInstant, retrievedToken.createdAt)

            // finally: clear data
            clearData(jdbi, "dbo.Tokens", "user_id", user.id)
            clearData(jdbi, "dbo.Users", "id", user.id)
        }


    @Test
    fun `update token`() =
        runWithHandle(jdbi) { handle ->
            // given: a UsersRepository
            val repo = JdbiUsersRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: a user is created
            val userName = newTestUserName()
            val email = newTestEmail(userName)
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            repo.storeUser(userName, email, passwordValidationInfo)

            // then: the user is created
            val user = repo.getUserByUsername(userName)
            assertNotNull(user)

            // and: test TokenValidationInfo
            val testTokenValidationInfo = TokenValidationInfo(newTokenValidationData())

            // when: creating a token
            val tokenCreationInstant = clock.now()
            val token = Token(
                testTokenValidationInfo,
                user.id,
                createdAt = tokenCreationInstant,
                lastUsedAt = tokenCreationInstant,
            )
            repo.createToken(token, 1)

            // then: createToken does not throw errors
            // no exception

            // when: updating the token
            val newInstant = Instant.fromEpochSeconds(tokenCreationInstant.epochSeconds + 1)
            val newToken = Token(token.tokenValidationInfo, token.userId, token.createdAt, newInstant)
            repo.updateTokenLastUsed(newToken, newInstant)

            // then: updateTokenLastUsed does not throw errors
            // no exception

            // when: retrieving the token and associated user
            val userAndToken = repo.getTokenByTokenValidationInfo(testTokenValidationInfo)

            // then:
            val (associatedUser, retrievedToken) = userAndToken ?: fail("token and associated user must exist")

            // and: ...
            assertEquals(userName, associatedUser.username)
            assertEquals(testTokenValidationInfo.validationInfo, retrievedToken.tokenValidationInfo.validationInfo)
            assertEquals(tokenCreationInstant, retrievedToken.createdAt)
            assertEquals(newInstant, retrievedToken.lastUsedAt)

            // finally: clear data
            clearData(jdbi, "dbo.Tokens", "user_id", user.id)
            clearData(jdbi, "dbo.Users", "id", user.id)

        }

    @Test
    fun `delete token`() =
        runWithHandle(jdbi) { handle ->
            // given: a UsersRepository
            val repo = JdbiUsersRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: a user is created
            val userName = newTestUserName()
            val email = newTestEmail(userName)
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            repo.storeUser(userName, email, passwordValidationInfo)

            // then: the user is created
            val user = repo.getUserByUsername(userName)
            assertNotNull(user)

            // and: test TokenValidationInfo
            val testTokenValidationInfo = TokenValidationInfo(newTokenValidationData())

            // when: creating a token
            val tokenCreationInstant = clock.now()
            val token = Token(
                testTokenValidationInfo,
                user.id,
                createdAt = tokenCreationInstant,
                lastUsedAt = tokenCreationInstant,
            )
            repo.createToken(token, 1)

            // then: createToken does not throw errors
            // no exception

            // when: deleting the token
            repo.removeTokenByValidationInfo(testTokenValidationInfo)

            // then: deleteToken does not throw errors
            // no exception

            // when: retrieving the token and associated user
            val userAndToken = repo.getTokenByTokenValidationInfo(testTokenValidationInfo)

            // then:
            assertEquals(null, userAndToken)

            // finally: clear data
            clearData(jdbi, "dbo.Users", "id", user.id)

        }

    @Test
    fun `create and validate invite register code`() =
        runWithHandle(jdbi) { handle ->
            // given: a UsersRepository
            val repo = JdbiUsersRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: a user is created
            val userName = newTestUserName()
            val email = newTestEmail(userName)
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            repo.storeUser(userName, email, passwordValidationInfo)

            // then: the user is created
            val user = repo.getUserByUsername(userName)
            assertNotNull(user)

            // and: test TokenValidationInfo
            val testTokenValidationInfo = TokenValidationInfo(newTokenValidationData())

            // when: creating a token
            val tokenCreationInstant = clock.now()
            val token = Token(
                testTokenValidationInfo,
                user.id,
                createdAt = tokenCreationInstant,
                lastUsedAt = tokenCreationInstant,
            )
            repo.createToken(token, 1)

            // then: createToken does not throw errors
            // no exception

            // when: creating an invite register code
            val code = generateInvitation()
            val register = RegisterModel(user.id, code, false)
            repo.createRegisterInvite(register)

            // then: createInviteRegisterCode does not throw errors
            // no exception

            // when: validating the invite register code
            val validatedUserId = repo.codeValidation(code)

            // then: validateInviteRegisterCode does not throw errors
            // no exception

            // and: ...
            assertNotNull(validatedUserId)
            assertEquals(code, validatedUserId.codHash)
            assertFalse(validatedUserId.expired)

            // finally: clear data
            clearData(jdbi, "dbo.Invitation_Register", "user_id", user.id)
            clearData(jdbi, "dbo.Users", "id", user.id)
        }


    @Test
    fun `invalidate invite code`() =
        runWithHandle(jdbi) { handle ->
            // given: a UsersRepository
            val repo = JdbiUsersRepository(handle)

            // and: a test clock
            val clock = TestClock()

            // when: a user is created
            val userName = newTestUserName()
            val email = newTestEmail(userName)
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            repo.storeUser(userName, email, passwordValidationInfo)

            // then: the user is created
            val user = repo.getUserByUsername(userName)
            assertNotNull(user)

            // and: test TokenValidationInfo
            val testTokenValidationInfo = TokenValidationInfo(newTokenValidationData())

            // when: creating a token
            val tokenCreationInstant = clock.now()
            val token = Token(
                testTokenValidationInfo,
                user.id,
                createdAt = tokenCreationInstant,
                lastUsedAt = tokenCreationInstant,
            )
            repo.createToken(token, 1)

            // then: createToken does not throw errors
            // no exception

            // when: creating an invitation register code
            val code = generateInvitation()
            val register = RegisterModel(user.id, code, false)
            repo.createRegisterInvite(register)

            // then: does not throw errors
            // no exception

            // when: invalidating the invite register code
            repo.invalidateCode(code)

            // then: does not throw errors
            // no exception

            // when: validating the invite register code
            val validatedUserId = repo.codeValidation(code)

            // then: validateInviteRegisterCode does not throw errors
            // no exception

            // and: invite code is invalidated
            assertNotNull(validatedUserId)
            assertEquals(code, validatedUserId.codHash)
            assertTrue(validatedUserId.expired)

            // finally: clear data
            clearData(jdbi, "dbo.Users", "id", user.id)
        }


    companion object {

        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

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
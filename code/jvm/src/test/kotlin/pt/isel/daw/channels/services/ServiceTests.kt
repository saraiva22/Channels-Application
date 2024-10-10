package pt.isel.daw.channels.services

import org.junit.jupiter.api.AfterAll
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.daw.channels.ApplicationTests
import pt.isel.daw.channels.TestClock
import pt.isel.daw.channels.clearChannelsDataByType
import pt.isel.daw.channels.clearData
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.domain.token.Sha256TokenEncoder
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.domain.user.UsersDomain
import pt.isel.daw.channels.domain.user.UsersDomainConfig
import pt.isel.daw.channels.repository.jdbi.JdbiTransactionManager
import pt.isel.daw.channels.services.channel.ChannelsService
import pt.isel.daw.channels.services.user.UsersService
import pt.isel.daw.channels.utils.Either
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

open class ServiceTests: ApplicationTests() {
    companion object {
        private fun createUsersService(
            testClock: TestClock,
            tokenTtl: Duration = 30.days,
            tokenRollingTtl: Duration = 30.minutes,
            maxTokensPerUser: Int = 3,
        ) = UsersService(
            JdbiTransactionManager(jdbi),
            UsersDomain(
                BCryptPasswordEncoder(),
                Sha256TokenEncoder(),
                UsersDomainConfig(
                    tokenSizeInBytes = 256 / 8,
                    tokenTtl = tokenTtl,
                    tokenRollingTtl,
                    maxTokensPerUser = maxTokensPerUser,
                ),
            ),
            testClock,
        )

        fun createChannelService() =
            ChannelsService(JdbiTransactionManager(jdbi), ChannelsDomain())

        var testUser: User

        init {
            val testClock = TestClock()
            val userServices = createUsersService(testClock)

            val userName = newTestUserName()
            val email = newTestEmail(userName)
            val password = newTokenValidationData()

            val userId = when (val createUserResult = userServices.createUser(userName, email, password)) {
                is Either.Left -> fail("Unexpected $createUserResult")
                is Either.Right -> createUserResult.value
            }

            val retrievedUser = when(val getUserResult = userServices.getUserById(userId)) {
                is Either.Left -> fail("Unexpected $getUserResult")
                is Either.Right -> getUserResult.value
            }

            testUser = retrievedUser
        }

        @JvmStatic
        @AfterAll
        fun clearDB(): Unit {
            clearData(jdbi, "dbo.Invite_Private_Channels", "user_id", testUser.id)
            clearChannelsDataByType(jdbi, "dbo.Public_Channels", testUser.id)
            clearChannelsDataByType(jdbi, "dbo.Private_Channels", testUser.id)
            clearData(jdbi, "dbo.Channels", "owner_id", testUser.id)
            clearData(jdbi, "dbo.Users", "id", testUser.id)
        }
    }
}
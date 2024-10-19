package pt.isel.daw.channels.services

import org.junit.jupiter.api.AfterAll
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.daw.channels.ApplicationTests
import pt.isel.daw.channels.TestClock
import pt.isel.daw.channels.clearChannelsDataByType
import pt.isel.daw.channels.clearData
import pt.isel.daw.channels.clearInvitationChannelsData
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.domain.messages.MessageDomain
import pt.isel.daw.channels.domain.token.Sha256TokenEncoder
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.domain.user.UsersDomain
import pt.isel.daw.channels.domain.user.UsersDomainConfig
import pt.isel.daw.channels.repository.jdbi.JdbiTransactionManager
import pt.isel.daw.channels.services.channel.ChannelsService
import pt.isel.daw.channels.services.message.MessagesService
import pt.isel.daw.channels.services.user.UsersService
import pt.isel.daw.channels.utils.Failure
import pt.isel.daw.channels.utils.Success
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

        fun createMessageService() =
            MessagesService(JdbiTransactionManager(jdbi), ChannelsDomain(), MessageDomain(), testClock)

        lateinit var testUser: User
        var testUser2: User
        lateinit var randomUser: User

        private val testClock = TestClock()
        private val userServices = createUsersService(testClock)
        private val channelServices = createChannelService()

        init {
            testUser = createUser()
            testUser2 = createUser()
        }

        private fun createUser(): User {
            val userName = newTestUserName()
            val email = newTestEmail(userName)
            val password = newTokenValidationData()
            val invitationCode = generateInvitationCode()

            return createUserInService(userName, email, password, invitationCode)
        }

        private fun generateInvitationCode(): String? {
            return if (!::testUser.isInitialized) {
                val hasUsers = channelServices.dbHasUsers()
                if (hasUsers) {
                    randomUser = userServices.getRandomUser() ?: fail("No random user found")
                    return userServices.createRegisterInvite(randomUser.id)
                }
                else {
                    null
                }
            } else {
                userServices.createRegisterInvite(testUser.id)
            }
        }

        private fun createUserInService(
            userName: String,
            email: String,
            password: String,
            invitationCode: String?
        ): User {
            val createUserResult = userServices.createUser(userName, email, password, invitationCode)
            val userId = when (createUserResult) {
                is Failure -> {
                    fail("Unexpected $createUserResult")
                }
                is Success -> createUserResult.value
            }

            val getUserResult = userServices.getUserById(userId)
            return when (getUserResult) {
                is Failure -> {
                    fail("Unexpected $getUserResult")
                }
                is Success -> getUserResult.value
            }
        }

        @JvmStatic
        @AfterAll
        fun clearDB(): Unit {
            clearData(jdbi, "dbo.Messages", "user_id", testUser.id)
            clearData(jdbi, "dbo.Messages", "user_id", testUser2.id)
            clearInvitationChannelsData(jdbi, testUser.id)
            clearInvitationChannelsData(jdbi, testUser2.id)
            if (::randomUser.isInitialized) clearInvitationChannelsData(jdbi, randomUser.id)
            clearChannelsDataByType(jdbi, "dbo.Public_Channels", testUser.id)
            clearChannelsDataByType(jdbi, "dbo.Public_Channels", testUser2.id)
            clearChannelsDataByType(jdbi, "dbo.Private_Channels", testUser.id)
            clearChannelsDataByType(jdbi, "dbo.Private_Channels", testUser2.id)
            clearData(jdbi, "dbo.Join_Channels", "user_id", testUser.id)
            clearData(jdbi, "dbo.Join_Channels", "user_id", testUser2.id)
            clearData(jdbi, "dbo.Channels", "owner_id", testUser.id)
            clearData(jdbi, "dbo.Channels", "owner_id", testUser2.id)
            clearData(jdbi, "dbo.Users", "id", testUser.id)
            clearData(jdbi, "dbo.Users", "id", testUser2.id)
        }
    }
}
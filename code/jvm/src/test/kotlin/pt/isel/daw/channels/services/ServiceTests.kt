package pt.isel.daw.channels.services

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.daw.channels.ApplicationTests
import pt.isel.daw.channels.TestClock
import pt.isel.daw.channels.clearData
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.domain.messages.MessageDomain
import pt.isel.daw.channels.domain.token.Sha256TokenEncoder
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.domain.user.UserInfo
import pt.isel.daw.channels.domain.user.UsersDomain
import pt.isel.daw.channels.domain.user.UsersDomainConfig
import pt.isel.daw.channels.repository.jdbi.JdbiTransactionManager
import pt.isel.daw.channels.services.channel.ChannelsService
import pt.isel.daw.channels.services.message.ChatService
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
        fun createUsersService(
            chatService: ChatService,
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
            chatService,
            testClock
        )

        fun createChannelService() =
            ChannelsService(JdbiTransactionManager(jdbi), ChannelsDomain(), chatService)

        fun createMessageService() =
            MessagesService(JdbiTransactionManager(jdbi), ChannelsDomain(), MessageDomain(), chatService, testClock)

        private val chatService = ChatService()
        private val testClock = TestClock()
        private val userServices = createUsersService(chatService, testClock)
        private val channelServices = createChannelService()

        lateinit var testUser: User
        private lateinit var testUser2: User
        private lateinit var randomUser: User

        lateinit var testUserInfo: UserInfo
        lateinit var testUserInfo2: UserInfo

        @JvmStatic
        @BeforeAll
        fun setupDB() {
            val hasUsers = channelServices.dbHasUsers()
            if (hasUsers) {
                randomUser = userServices.getRandomUser() ?: fail("No random user found")
            }
            testUser = createUser()
            testUser2 = createUser()
            testUserInfo = UserInfo(testUser.id, testUser.username, testUser.email)
            testUserInfo2 = UserInfo(testUser2.id, testUser2.username, testUser2.email)
        }

        private fun createUser(): User {
            val userName = newTestUserName()
            val email = newTestEmail(userName)
            val password = newTestPassword()
            val invitationCode = generateInvitationCode()

            return createUserInService(userName, email, password, invitationCode)
        }

        fun generateInvitationCode(): String? {
            return if (::randomUser.isInitialized) {
                userServices.createRegisterInvite(randomUser.id)
            } else if (!::testUser.isInitialized) {
                null
            } else {
                userServices.createRegisterInvite(testUser.id)
            }
        }

        fun createUserInService(
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
            clearData(jdbi, "dbo.Invitation_Channels", "inviter_id", testUser.id)
            clearData(jdbi, "dbo.Invitation_Channels", "inviter_id", testUser2.id)
            if (::randomUser.isInitialized)
                clearData(jdbi, "dbo.Invitation_Channels", "inviter_id", randomUser.id)
            clearData(jdbi, "dbo.Join_Channels", "user_id", testUser.id)
            clearData(jdbi, "dbo.Join_Channels", "user_id", testUser2.id)
            clearData(jdbi, "dbo.Channels", "owner_id", testUser.id)
            clearData(jdbi, "dbo.Channels", "owner_id", testUser2.id)
            clearData(jdbi, "dbo.Users", "id", testUser.id)
            clearData(jdbi, "dbo.Users", "id", testUser2.id)
        }
    }
}
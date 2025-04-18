package pt.isel.daw.channels.repository.jdbi

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import pt.isel.daw.channels.ApplicationTests
import pt.isel.daw.channels.clearData
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.domain.user.PasswordValidationInfo
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.domain.user.UserInfo
import pt.isel.daw.channels.runWithHandle


open class RepositoryTests: ApplicationTests() {
    companion object {
        val channelsDomain = ChannelsDomain()

        private lateinit var testUser: User
        private lateinit var testUser2: User

        lateinit var testUserInfo: UserInfo
        lateinit var testUserInfo2: UserInfo


        @JvmStatic
        @BeforeAll
        fun setupDB() {
            runWithHandle(jdbi) { handle ->
                val repo = JdbiUsersRepository(handle)
                testUser = createUser(repo)
                testUser2 = createUser(repo)
                testUserInfo = UserInfo(testUser.id, testUser.username, testUser.email)
                testUserInfo2 = UserInfo(testUser2.id, testUser2.username, testUser2.email)
            }
        }

        private fun createUser(repo: JdbiUsersRepository): User {
            val userName = newTestUserName()
            val email = newTestEmail(userName)
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            repo.storeUser(userName, email, passwordValidationInfo)
            return repo.getUserByUsername(userName) ?: throw IllegalStateException("User creation failed")
        }

        @JvmStatic
        @AfterAll
        fun clearDB(): Unit {
            clearData(jdbi, "dbo.Messages", "user_id", testUser.id)
            clearData(jdbi, "dbo.Messages", "user_id", testUser2.id)
            clearData(jdbi, "dbo.Invitation_Channels", "inviter_id", testUser.id)
            clearData(jdbi, "dbo.Invitation_Channels", "inviter_id", testUser2.id)
            clearData(jdbi, "dbo.Join_Channels", "user_id", testUser.id)
            clearData(jdbi, "dbo.Join_Channels", "user_id", testUser2.id)
            clearData(jdbi, "dbo.Channels", "owner_id", testUser.id)
            clearData(jdbi, "dbo.Channels", "owner_id", testUser2.id)
            clearData(jdbi, "dbo.Users", "id", testUser.id)
            clearData(jdbi, "dbo.Users", "id", testUser2.id)
        }
    }
}
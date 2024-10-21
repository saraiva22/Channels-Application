package pt.isel.daw.channels.repository.jdbi

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import pt.isel.daw.channels.ApplicationTests
import pt.isel.daw.channels.clearChannelsDataByType
import pt.isel.daw.channels.clearData
import pt.isel.daw.channels.clearInvitationChannelsData
import pt.isel.daw.channels.domain.channels.ChannelsDomain
import pt.isel.daw.channels.domain.user.PasswordValidationInfo
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.runWithHandle

open class RepositoryTests: ApplicationTests() {
    companion object {
        val channelsDomain = ChannelsDomain()

        lateinit var testUser: User
        lateinit var testUser2: User

        @JvmStatic
        @BeforeAll
        fun setupDB() {
            runWithHandle(jdbi) { handle ->
                val repo = JdbiUsersRepository(handle)
                testUser = createUser(repo)
                testUser2 = createUser(repo)
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
            clearInvitationChannelsData(jdbi, testUser.id)
            clearInvitationChannelsData(jdbi, testUser2.id)
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
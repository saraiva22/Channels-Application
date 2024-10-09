package pt.isel.daw.channels.repository.jdbi

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterAll
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.daw.channels.ApplicationTests
import pt.isel.daw.channels.clearChannelsDataByType
import pt.isel.daw.channels.clearData
import pt.isel.daw.channels.domain.user.PasswordValidationInfo
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.repository.configureWithAppRequirements
import pt.isel.daw.channels.runWithHandle

open class RepositoryTests: ApplicationTests() {
    companion object {
        lateinit var testUser: User

        val jdbi =
            Jdbi.create(
                PGSimpleDataSource().apply {
                    setURL("jdbc:postgresql://localhost/?user=postgres&password=daw")
                },
            ).configureWithAppRequirements()

        init {
            runWithHandle(jdbi) { handle ->
                val repo = JdbiUsersRepository(handle)

                val userName = newTestUserName()
                val email = newTestEmail(userName)
                val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
                repo.storeUser(userName, email, passwordValidationInfo)

                val retrievedUser: User? = repo.getUserByUsername(userName)

                if (retrievedUser != null) testUser = retrievedUser
            }
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
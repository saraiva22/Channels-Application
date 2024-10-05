package pt.isel.daw.channels.repository

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.daw.channels.Environment
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.domain.user.PasswordValidationInfo
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.repository.Utils.configureWithAppRequirements
import pt.isel.daw.channels.repository.jdbi.JdbiChannelsRepository
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertNotNull

class JdbiChannelsRepositoryTests {
    /*
    @Test
    fun `can create and retrieve user`() =
        runWithHandle { handle ->
            // given: a UsersRepository
            val repo = JdbiChannelsRepository(handle)

            // when: storing a user
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            val userName = User(1, "carolina@gmail.com", "carol", passwordValidationInfo)
            val channel = ChannelModel(userName, "ch1", "no rules", Type.PUBLIC)
            val chID = repo.createChannel(channel)


        }

    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private fun newTestUserName() = "user-${abs(Random.nextLong())}"

        private fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

        private val jdbi =
            Jdbi.create(
                PGSimpleDataSource().apply {
                    setURL(Environment.getDbUrl())
                },
            ).configureWithAppRequirements()
    }

     */
}
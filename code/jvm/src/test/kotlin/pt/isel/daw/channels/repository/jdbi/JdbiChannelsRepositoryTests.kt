package pt.isel.daw.channels.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.domain.user.PasswordValidationInfo
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.repository.configureWithAppRequirements
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JdbiChannelsRepositoryTests {

    private lateinit var testUser: User

    @BeforeTest
    fun `create user for channels test`() {
        runWithHandle { handle ->
            val repo = JdbiUsersRepository(handle)

            val userName = newTestUserName()
            val email = newTestEmail(userName)
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            repo.storeUser(userName, email, passwordValidationInfo)

            val retrievedUser: User? = repo.getUserByUsername(userName)

            if (retrievedUser != null) testUser = retrievedUser
        }
    }

    @Test
    fun `can create and retrieve public channel`() {
        runWithHandle { handle ->
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(handle)

            // when: storing a public channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUser.id, Type.PUBLIC)
            repo.createChannel(channel)

            // and: retrieving a channel
            val retrievedChannel: Channel? = repo.getChannelByName(channelName)

            // then:
            assertNotNull(retrievedChannel)
            assertEquals(channelName, retrievedChannel.name)
            assertEquals(channel.owner, retrievedChannel.owner)
            assertTrue(retrievedChannel.id >= 0)
            assertTrue(retrievedChannel.members.isEmpty())

            // when: asking if the channel is public
            val ifChannelIsPublic = repo.isChannelPublic(retrievedChannel)

            // then: response is true
            assertTrue(ifChannelIsPublic)

            // when: asking if the channel exists
            val ifChannelIsStored = repo.isChannelStoredByName(channelName)

            // then: response is true
            assertTrue(ifChannelIsStored)

            // when: asking if a different channel exists
            val anotherChannelIsStored = repo.isChannelStoredByName("another-$channelName")

            // then: response is false
            assertFalse(anotherChannelIsStored)
        }
    }

    @Test
    fun `create private channel`() {
        runWithHandle { handle ->
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(handle)

            // when: storing a public channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUser.id, Type.PRIVATE)
            repo.createChannel(channel)

            // and: retrieving a channel
            val retrievedChannel: Channel? = repo.getChannelByName(channelName)

            // then:
            assertNotNull(retrievedChannel)
            assertEquals(channelName, retrievedChannel.name)
            assertEquals(channel.owner, retrievedChannel.owner)
            assertTrue(retrievedChannel.id >= 0)
            assertTrue(retrievedChannel.members.isEmpty())

            // when: asking if the channel is public
            val ifChannelIsPublic = repo.isChannelPublic(retrievedChannel)

            // then: response is false
            assertFalse(ifChannelIsPublic)

            // when: asking if the channel exists
            val ifChannelIsStored = repo.isChannelStoredByName(channelName)

            // then: response is true
            assertTrue(ifChannelIsStored)

            // when: asking if a different channel exists
            val anotherChannelIsStored = repo.isChannelStoredByName("another-$channelName")

            // then: response is false
            assertFalse(anotherChannelIsStored)
        }
    }

    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private fun newTestUserName() = "user-${abs(Random.nextLong())}"

        private fun newTestEmail(username: String) = "$username@testmail.com"

        private fun newTestChannelName() = "channel-${abs(Random.nextLong())}"

        private fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

        private val jdbi =
            Jdbi.create(
                PGSimpleDataSource().apply {
                    setURL("jdbc:postgresql://localhost/?user=postgres&password=daw")
                },
            ).configureWithAppRequirements()
    }
}
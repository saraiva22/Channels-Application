package pt.isel.daw.channels.repository.jdbi

import org.junit.jupiter.api.Test
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.runWithHandle
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JdbiChannelsRepositoryTests: RepositoryTests() {

    @Test
    fun `can create and retrieve public channel`() {
        runWithHandle(jdbi) { handle ->
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(handle)

            // when: storing a public channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUser.id, Type.PUBLIC)
            repo.createChannel(channel)

            // and: retrieving a channel
            val retrievedChannel: Channel? = repo.searchChannelsByName(channelName)

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
        runWithHandle(jdbi) { handle ->
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(handle)

            // when: storing a public channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUser.id, Type.PRIVATE)
            repo.createChannel(channel)

            // and: retrieving a channel
            val retrievedChannel: Channel? = repo.searchChannelsByName(channelName)

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
}
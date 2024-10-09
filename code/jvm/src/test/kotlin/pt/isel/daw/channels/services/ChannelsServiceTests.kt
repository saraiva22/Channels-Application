package pt.isel.daw.channels.services

import org.junit.jupiter.api.Test
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.utils.Either
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class ChannelsServiceTests: ServiceTests() {

    @Test
    fun `can create, retrieve a public channel by id and name and check channel type`() {

        // given: a channel service
        val service = createChannelService()

        // when: creating a channel
        val channelName = newTestChannelName()
        val channelModel = ChannelModel(channelName, testUser.id, Type.PUBLIC)
        val createChannelResult = service.createChannel(channelModel)

        // then: the creation is successful
        when (createChannelResult) {
            is Either.Left -> fail("Unexpected $createChannelResult")
            is Either.Right -> assertTrue(createChannelResult.value > 0)
        }

        // when: using the id
        val getChannelByIdResult = service.getChannelById(createChannelResult.value)

        // then: the return is successful and has the same id, name, owner and empty members list
        when (getChannelByIdResult) {
            is Either.Left -> fail("Unexpected $getChannelByIdResult")
            is Either.Right -> {
                assertEquals(createChannelResult.value, getChannelByIdResult.value.id)
                assertEquals(channelName, getChannelByIdResult.value.name)
                assertEquals(testUser.id, getChannelByIdResult.value.owner)
                assertEquals(emptyList(), getChannelByIdResult.value.members)
            }
        }

        // when: using the name
        val getChannelByNameResult = service.getChannelByName(getChannelByIdResult.value.name)

        // then: the return is successful and has the same id, name, owner and empty members list
        when (getChannelByNameResult) {
            is Either.Left -> fail("Unexpected $getChannelByNameResult")
            is Either.Right -> {
                assertEquals(createChannelResult.value, getChannelByNameResult.value.id)
                assertEquals(channelName, getChannelByNameResult.value.name)
                assertEquals(testUser.id, getChannelByNameResult.value.owner)
                assertEquals(emptyList(), getChannelByNameResult.value.members)
            }
        }

        // and: both returned channels are the same
        assertEquals(getChannelByIdResult, getChannelByNameResult)

        // when: getting the list of public channels
        val publicChannels = service.getPublicChannels()

        // then: the channel is in the public channels list
        assertTrue(publicChannels.contains(getChannelByIdResult.value))
        assertTrue(publicChannels.contains(getChannelByNameResult.value))
    }

    @Test
    fun `can create, retrieve a private channel by id and name and check channel type`() {

        // given: a channel service
        val service = createChannelService()

        // when: creating a channel
        val channelName = newTestChannelName()
        val channelModel = ChannelModel(channelName, testUser.id, Type.PRIVATE)
        val createChannelResult = service.createChannel(channelModel)

        // then: the creation is successful
        when (createChannelResult) {
            is Either.Left -> fail("Unexpected $createChannelResult")
            is Either.Right -> assertTrue(createChannelResult.value > 0)
        }

        // when: using the id
        val getChannelByIdResult = service.getChannelById(createChannelResult.value)

        // then: the return is successful and has the same id, name, owner and empty members list
        when (getChannelByIdResult) {
            is Either.Left -> fail("Unexpected $getChannelByIdResult")
            is Either.Right -> {
                assertEquals(createChannelResult.value, getChannelByIdResult.value.id)
                assertEquals(channelName, getChannelByIdResult.value.name)
                assertEquals(testUser.id, getChannelByIdResult.value.owner)
                assertEquals(emptyList(), getChannelByIdResult.value.members)
            }
        }

        // when: using the name
        val getChannelByNameResult = service.getChannelByName(getChannelByIdResult.value.name)

        // then: the return is successful and has the same id, name, owner and empty members list
        when (getChannelByNameResult) {
            is Either.Left -> fail("Unexpected $getChannelByNameResult")
            is Either.Right -> {
                assertEquals(createChannelResult.value, getChannelByNameResult.value.id)
                assertEquals(channelName, getChannelByNameResult.value.name)
                assertEquals(testUser.id, getChannelByNameResult.value.owner)
                assertEquals(emptyList(), getChannelByNameResult.value.members)
            }
        }

        // and: both returned channels are the same
        assertEquals(getChannelByIdResult, getChannelByNameResult)

        // when: getting the list of public channels
        val publicChannels = service.getPublicChannels()

        // then: the channel is not in the public channels list
        assertFalse(publicChannels.contains(getChannelByIdResult.value))
        assertFalse(publicChannels.contains(getChannelByNameResult.value))
    }
}
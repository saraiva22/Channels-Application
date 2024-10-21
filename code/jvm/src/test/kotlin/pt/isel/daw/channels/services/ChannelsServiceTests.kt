package pt.isel.daw.channels.services

import org.junit.jupiter.api.Test
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.repository.jdbi.RepositoryTests.Companion.testUserInfo
import pt.isel.daw.channels.repository.jdbi.RepositoryTests.Companion.testUserInfo2
import pt.isel.daw.channels.utils.Either
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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
        val getChannelByIdResult = service.getChannelById(testUser.id, createChannelResult.value)

        // then: the return is successful and has the same id, name, owner and empty members list
        when (getChannelByIdResult) {
            is Either.Left -> fail("Unexpected $getChannelByIdResult")
            is Either.Right -> {
                assertEquals(createChannelResult.value, getChannelByIdResult.value.id)
                assertEquals(channelName, getChannelByIdResult.value.name)
                assertEquals(testUserInfo, getChannelByIdResult.value.owner)
                assertTrue(getChannelByIdResult.value.members.size == 1)
                assertTrue(getChannelByIdResult.value.members.contains(testUserInfo))
            }
        }

        // when: using the name
        val getChannelByNameResult = service.getChannelByName(testUser.id, channelName, null)

        // then: the return is successful and has the same id, name, owner and empty members list
        assertTrue(getChannelByNameResult.size == 1)
        val createdChannel = getChannelByNameResult[0]
        assertEquals(channelName, createdChannel.name)
        assertEquals(testUserInfo, createdChannel.owner)
        assertTrue(createdChannel.members.size == 1)
        assertEquals(createdChannel.members[0], createdChannel.owner)

        // and: both returned channels are the same
        assertEquals(getChannelByIdResult.value, createdChannel)

        // when: getting the list of public channels
        val publicChannels = service.getPublicChannels(null)

        // then: the channel is in the public channels list
        assertTrue(publicChannels.contains(getChannelByIdResult.value))
        assertTrue(publicChannels.contains(createdChannel))
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
        val getChannelByIdResult = service.getChannelById(testUser.id, createChannelResult.value)

        // then: the return is successful and has the same id, name, owner and empty members list
        when (getChannelByIdResult) {
            is Either.Left -> fail("Unexpected $getChannelByIdResult")
            is Either.Right -> {
                assertEquals(createChannelResult.value, getChannelByIdResult.value.id)
                assertEquals(channelName, getChannelByIdResult.value.name)
                assertEquals(testUserInfo, getChannelByIdResult.value.owner)
                assertTrue(getChannelByIdResult.value.members.size == 1)
                assertTrue(getChannelByIdResult.value.members.contains(testUserInfo))
            }
        }

        // when: using the name
        val getChannelByNameResult = service.getChannelByName(testUser.id, channelName, null)

        // then: the return is successful and has the same id, name, owner and empty members list
        assertTrue(getChannelByNameResult.size == 1)
        val createdChannel = getChannelByNameResult[0]
        assertEquals(channelName, createdChannel.name)
        assertEquals(testUserInfo, createdChannel.owner)
        assertTrue(createdChannel.members.size == 1)
        assertEquals(createdChannel.members[0], createdChannel.owner)

        // and: both returned channels are the same
        assertEquals(getChannelByIdResult.value, createdChannel)

        // when: getting the list of public channels
        val publicChannels = service.getPublicChannels(null)

        // then: the channel is not in the public channels list
        assertFalse(publicChannels.contains(getChannelByIdResult.value))
        assertFalse(publicChannels.contains(createdChannel))
    }

    @Test
    fun `get channels owned by the user`() {
        // given: a channel service
        val service = createChannelService()

        // when: creating a public channel
        val publicChannelName = newTestChannelName()
        val publicChannelModel = ChannelModel(publicChannelName, testUser.id, Type.PUBLIC)
        val createPublicChannelResult = service.createChannel(publicChannelModel)

        // then: the creation is successful
        when (createPublicChannelResult) {
            is Either.Left -> fail("Unexpected $createPublicChannelResult")
            is Either.Right -> assertTrue(createPublicChannelResult.value > 0)
        }

        // when: creating a private channel
        val privateChannelName = newTestChannelName()
        val privateChannelModel = ChannelModel(privateChannelName, testUser.id, Type.PRIVATE)
        val createPrivateChannelResult = service.createChannel(privateChannelModel)

        // then: the creation is successful
        when (createPrivateChannelResult) {
            is Either.Left -> fail("Unexpected $createPrivateChannelResult")
            is Either.Right -> assertTrue(createPrivateChannelResult.value > 0)
        }

        // when: getting the list of channels owned by the user
        val ownedChannels = service.getUserOwnedChannels(testUser.id, null)

        // and: getting the channels by id
        val publicChannel = service.getChannelById(testUser.id, createPublicChannelResult.value)
        val privateChannel = service.getChannelById(testUser.id, createPrivateChannelResult.value)

        // then: the get is successful and the list contains the public and private channels
        when (publicChannel) {
            is Either.Left -> fail("Unexpected $publicChannel")
            is Either.Right -> assertEquals(createPublicChannelResult.value, publicChannel.value.id)
        }
        when (privateChannel) {
            is Either.Left -> fail("Unexpected $privateChannel")
            is Either.Right -> assertEquals(createPrivateChannelResult.value, privateChannel.value.id)
        }
        assertTrue(ownedChannels.contains(publicChannel.value))
        assertTrue(ownedChannels.contains(privateChannel.value))
    }

    @Test
    fun `get public channels`() {
        // given: a channel service
        val service = createChannelService()

        // when: creating a public channel
        val publicChannelName = newTestChannelName()
        val publicChannelModel = ChannelModel(publicChannelName, testUser.id, Type.PUBLIC)
        val createPublicChannelResult = service.createChannel(publicChannelModel)

        // then: the creation is successful
        when (createPublicChannelResult) {
            is Either.Left -> fail("Unexpected $createPublicChannelResult")
            is Either.Right -> assertTrue(createPublicChannelResult.value > 0)
        }

        // when: creating a private channel
        val privateChannelName = newTestChannelName()
        val privateChannelModel = ChannelModel(privateChannelName, testUser.id, Type.PRIVATE)
        val createPrivateChannelResult = service.createChannel(privateChannelModel)

        // then: the creation is successful
        when (createPrivateChannelResult) {
            is Either.Left -> fail("Unexpected $createPrivateChannelResult")
            is Either.Right -> assertTrue(createPrivateChannelResult.value > 0)
        }

        // when: getting the list of public channels
        val publicChannels = service.getPublicChannels(null)

        // and: getting the channels by id
        val publicChannel = service.getChannelById(testUser.id, createPublicChannelResult.value)
        val privateChannel = service.getChannelById(testUser.id, createPrivateChannelResult.value)

        // then: the get is successful and the list contains the public channel
        when (publicChannel) {
            is Either.Left -> fail("Unexpected $publicChannel")
            is Either.Right -> assertEquals(createPublicChannelResult.value, publicChannel.value.id)
        }
        when (privateChannel) {
            is Either.Left -> fail("Unexpected $privateChannel")
            is Either.Right -> assertEquals(createPrivateChannelResult.value, privateChannel.value.id)
        }
        assertTrue(publicChannels.contains(publicChannel.value))
        assertFalse(publicChannels.contains(privateChannel.value))
    }

    @Test
    fun `updating channel name`() {
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

        // when: updating the channel name
        val newChannelName = newTestChannelName()
        val updateChannelResult = service.updateNameChannel(newChannelName, createChannelResult.value, testUser.id)

        // then: the update is successful
        when (updateChannelResult) {
            is Either.Left -> fail("Unexpected $updateChannelResult")
            is Either.Right -> {
                assertEquals(createChannelResult.value, updateChannelResult.value.id)
                assertNotEquals(channelName, updateChannelResult.value.name)
                assertEquals(newChannelName, updateChannelResult.value.name)
                assertEquals(testUserInfo, updateChannelResult.value.owner)
                assertTrue(updateChannelResult.value.members.size == 1)
                assertTrue(updateChannelResult.value.members.contains(testUserInfo))
            }
        }

        // when: getting the channel by id
        val getChannelByIdResult = service.getChannelById(testUser.id, createChannelResult.value)

        // then: the return is successful and has the new name
        when (getChannelByIdResult) {
            is Either.Left -> fail("Unexpected $getChannelByIdResult")
            is Either.Right -> assertEquals(newChannelName, getChannelByIdResult.value.name)
        }

        // when: getting the channel by name
        val getChannelByNameResult = service.getChannelByName(testUser.id, newChannelName, null)

        // then: the return is successful and has the new name
        assertTrue(getChannelByNameResult.size == 1)
        val createdChannel = getChannelByNameResult[0]
        assertEquals(newChannelName, createdChannel.name)
    }

    @Test
    fun `user join public channel`() {
        // given: a channel service
        val service = createChannelService()

        // when: creating a public channel
        val publicChannelName = newTestChannelName()
        val publicChannelModel = ChannelModel(publicChannelName, testUser.id, Type.PUBLIC)
        val createPublicChannelResult = service.createChannel(publicChannelModel)

        // then: the creation is successful
        when (createPublicChannelResult) {
            is Either.Left -> fail("Unexpected $createPublicChannelResult")
            is Either.Right -> assertTrue(createPublicChannelResult.value > 0)
        }

        // when: user joins the public channel
        val joinChannelResult = service.joinUsersInPublicChannel(testUser2.id, createPublicChannelResult.value)

        // then: the join is successful
        when (joinChannelResult) {
            is Either.Left -> fail("Unexpected $joinChannelResult")
            is Either.Right -> {
                assertEquals(createPublicChannelResult.value, joinChannelResult.value.id)
                assertEquals(publicChannelName, joinChannelResult.value.name)
                assertEquals(testUserInfo, joinChannelResult.value.owner)
                assertTrue(joinChannelResult.value.members.size == 2)
                assertTrue(joinChannelResult.value.members.contains(testUserInfo))
                assertTrue(joinChannelResult.value.members.contains(testUserInfo2))
            }
        }

        // when: getting the channel by id
        val getChannelByIdResult = service.getChannelById(testUser.id, createPublicChannelResult.value)

        // then: the return is successful and has the new member
        when (getChannelByIdResult) {
            is Either.Left -> fail("Unexpected $getChannelByIdResult")
            is Either.Right -> {
                assertEquals(createPublicChannelResult.value, getChannelByIdResult.value.id)
                assertEquals(publicChannelName, getChannelByIdResult.value.name)
                assertEquals(testUserInfo, getChannelByIdResult.value.owner)
                assertTrue(getChannelByIdResult.value.members.size == 2)
                assertTrue(getChannelByIdResult.value.members.contains(testUserInfo))
                assertTrue(getChannelByIdResult.value.members.contains(testUserInfo2))
            }
        }

        // when: getting the channel by name
        val getChannelByNameResult = service.getChannelByName(testUser.id, publicChannelName, null)

        // then: the return is successful and has the new member
        assertTrue(getChannelByNameResult.size == 1)
        val createdChannel = getChannelByNameResult[0]
        assertEquals(publicChannelName, createdChannel.name)
        assertEquals(testUserInfo, createdChannel.owner)
    }

    @Test
    fun `user leave public channel`() {
        // given: a channel service
        val service = createChannelService()

        // when: creating a public channel
        val publicChannelName = newTestChannelName()
        val publicChannelModel = ChannelModel(publicChannelName, testUser.id, Type.PUBLIC)
        val createPublicChannelResult = service.createChannel(publicChannelModel)

        // then: the creation is successful
        when (createPublicChannelResult) {
            is Either.Left -> fail("Unexpected $createPublicChannelResult")
            is Either.Right -> assertTrue(createPublicChannelResult.value > 0)
        }

        // when: user joins the public channel
        val joinChannelResult = service.joinUsersInPublicChannel(testUser2.id, createPublicChannelResult.value)

        // then: the join is successful
        when (joinChannelResult) {
            is Either.Left -> fail("Unexpected $joinChannelResult")
            is Either.Right -> {
                assertEquals(createPublicChannelResult.value, joinChannelResult.value.id)
                assertEquals(publicChannelName, joinChannelResult.value.name)
                assertEquals(testUserInfo, joinChannelResult.value.owner)
                assertTrue(joinChannelResult.value.members.size == 2)
                assertTrue(joinChannelResult.value.members.contains(testUserInfo))
                assertTrue(joinChannelResult.value.members.contains(testUserInfo2))
            }
        }

        // when: user leaves the public channel
        val leaveChannelResult = service.leaveChannel(testUser2.id, createPublicChannelResult.value)

        // then: the leave is successful
        when (leaveChannelResult) {
            is Either.Left -> fail("Unexpected $leaveChannelResult")
            is Either.Right -> Unit
        }

        // when: getting the channel by id
        val getChannelByIdResult = service.getChannelById(testUser.id, createPublicChannelResult.value)

        // then: the return is successful and has the new member
        when (getChannelByIdResult) {
            is Either.Left -> fail("Unexpected $getChannelByIdResult")
            is Either.Right -> {
                assertEquals(createPublicChannelResult.value, getChannelByIdResult.value.id)
                assertEquals(publicChannelName, getChannelByIdResult.value.name)
                assertEquals(testUserInfo, getChannelByIdResult.value.owner)
                assertTrue(getChannelByIdResult.value.members.size == 1)
                assertTrue(getChannelByIdResult.value.members.contains(testUserInfo))
                assertFalse(getChannelByIdResult.value.members.contains(testUserInfo2))
            }
        }
    }

    @Test
    fun `user join a private channel`() {
        // given: a channel service
        val service = createChannelService()

        // when: creating a private channel
        val privateChannelName = newTestChannelName()
        val privateChannelModel = ChannelModel(privateChannelName, testUser.id, Type.PRIVATE)
        val createPrivateChannelResult = service.createChannel(privateChannelModel)

        // then: the creation is successful
        when (createPrivateChannelResult) {
            is Either.Left -> fail("Unexpected $createPrivateChannelResult")
            is Either.Right -> assertTrue(createPrivateChannelResult.value > 0)
        }

        // when: creating a code for the private channel
        val code = service.invitePrivateChannel(
            createPrivateChannelResult.value,
            testUser.id,
            testUser2.username,
            Privacy.READ_WRITE
        )

        // then: the creation is successful
        when (code) {
            is Either.Left -> fail("Unexpected $code")
            is Either.Right -> assertTrue(code.value.isNotEmpty())
        }

        // when: user joins the private channel
        val joinChannelResult = service.joinUsersInPrivateChannel(
            testUser2.id,
            createPrivateChannelResult.value,
            code.value
        )

        // then: the join is successful
        when (joinChannelResult) {
            is Either.Left -> fail("Unexpected $joinChannelResult")
            is Either.Right -> {
                assertEquals(createPrivateChannelResult.value, joinChannelResult.value.id)
                assertEquals(privateChannelName, joinChannelResult.value.name)
                assertEquals(testUserInfo, joinChannelResult.value.owner)
                assertTrue(joinChannelResult.value.members.size == 2)
                assertTrue(joinChannelResult.value.members.contains(testUserInfo))
                assertTrue(joinChannelResult.value.members.contains(testUserInfo2))
            }
        }

        // when: getting the channel by id
        val getChannelByIdResult = service.getChannelById(testUser.id, createPrivateChannelResult.value)

        // then: the return is successful and has the new member
        when (getChannelByIdResult) {
            is Either.Left -> fail("Unexpected $getChannelByIdResult")
            is Either.Right -> {
                assertEquals(createPrivateChannelResult.value, getChannelByIdResult.value.id)
                assertEquals(privateChannelName, getChannelByIdResult.value.name)
                assertEquals(testUserInfo, getChannelByIdResult.value.owner)
                assertTrue(getChannelByIdResult.value.members.size == 2)
                assertTrue(getChannelByIdResult.value.members.contains(testUserInfo))
                assertTrue(getChannelByIdResult.value.members.contains(testUserInfo2))
            }
        }
    }

    @Test
    fun `user leaves private channel`() {
        // given: a channel service
        val service = createChannelService()

        // when: creating a private channel
        val privateChannelName = newTestChannelName()
        val privateChannelModel = ChannelModel(privateChannelName, testUser.id, Type.PRIVATE)
        val createPrivateChannelResult = service.createChannel(privateChannelModel)

        // then: the creation is successful
        when (createPrivateChannelResult) {
            is Either.Left -> fail("Unexpected $createPrivateChannelResult")
            is Either.Right -> assertTrue(createPrivateChannelResult.value > 0)
        }

        // when: creating a code for the private channel
        val code = service.invitePrivateChannel(
            createPrivateChannelResult.value,
            testUser.id,
            testUser2.username,
            Privacy.READ_WRITE
        )

        // then: the creation is successful
        when (code) {
            is Either.Left -> fail("Unexpected $code")
            is Either.Right -> assertTrue(code.value.isNotEmpty())
        }

        // when: user joins the private channel
        val joinChannelResult = service.joinUsersInPrivateChannel(
            testUser2.id,
            createPrivateChannelResult.value,
            code.value
        )

        // then: the join is successful
        when (joinChannelResult) {
            is Either.Left -> fail("Unexpected $joinChannelResult")
            is Either.Right -> {
                assertEquals(createPrivateChannelResult.value, joinChannelResult.value.id)
                assertEquals(privateChannelName, joinChannelResult.value.name)
                assertEquals(testUserInfo, joinChannelResult.value.owner)
                assertTrue(joinChannelResult.value.members.size == 2)
                assertTrue(joinChannelResult.value.members.contains(testUserInfo))
                assertTrue(joinChannelResult.value.members.contains(testUserInfo2))
            }
        }

        // when: user leaves the private channel
        val leaveChannelResult = service.leaveChannel(testUser2.id, createPrivateChannelResult.value)

        // then: the leave is successful
        when (leaveChannelResult) {
            is Either.Left -> fail("Unexpected $leaveChannelResult")
            is Either.Right -> Unit
        }

        // when: getting the channel by id
        val getChannelByIdResult = service.getChannelById(testUser.id, createPrivateChannelResult.value)

        // then: the return is successful and has the new member
        when (getChannelByIdResult) {
            is Either.Left -> fail("Unexpected $getChannelByIdResult")
            is Either.Right -> {
                assertEquals(createPrivateChannelResult.value, getChannelByIdResult.value.id)
                assertEquals(privateChannelName, getChannelByIdResult.value.name)
                assertEquals(testUserInfo, getChannelByIdResult.value.owner)
                assertTrue(getChannelByIdResult.value.members.size == 1)
                assertTrue(getChannelByIdResult.value.members.contains(testUserInfo))
                assertFalse(getChannelByIdResult.value.members.contains(testUserInfo2))
            }
        }
    }
}
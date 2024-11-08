package pt.isel.daw.channels.repository.jdbi

import org.junit.jupiter.api.Test
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Privacy
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
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PUBLIC)
            val channelId = repo.createChannel(channel)

            // and: searching for the channel by name
            val retrievedChannel: List<Channel> = repo.searchChannelsByName(channelName, null)

            // then:
            assertTrue(retrievedChannel.size == 1)
            val createdChannel = retrievedChannel[0]
            assertEquals(channelName, createdChannel.name)
            assertEquals(testUserInfo, createdChannel.owner)
            assertTrue(createdChannel.members.size == 1)
            assertEquals(createdChannel.members[0], createdChannel.owner)

            // when: getting the channel by id
            val retrievedChannelById: Channel? = repo.getChannelById(channelId)

            // then:
            assertNotNull(retrievedChannelById)
            assertEquals(retrievedChannelById, createdChannel)

            // when: asking if the channel is public
            val ifChannelIsPublic = repo.isChannelPublic(createdChannel)

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
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PRIVATE)
            val channelId = repo.createChannel(channel)

            // and: searching for the channel by name
            val retrievedChannel: List<Channel> = repo.searchChannelsByName(channelName, null)

            // then:
            assertTrue(retrievedChannel.size == 1)
            val createdChannel = retrievedChannel[0]
            assertEquals(channelName, createdChannel.name)
            assertEquals(testUserInfo, createdChannel.owner)
            assertTrue(createdChannel.id >= 0)
            assertTrue(createdChannel.members.size == 1)
            assertEquals(createdChannel.members[0], createdChannel.owner)

            // when: getting the channel by id
            val retrievedChannelById: Channel? = repo.getChannelById(channelId)

            // then:
            assertNotNull(retrievedChannelById)
            assertEquals(retrievedChannelById, createdChannel)

            // when: asking if the channel is public
            val ifChannelIsPublic = repo.isChannelPublic(createdChannel)

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

    @Test
    fun `get channels owned by the user`() {
        runWithHandle(jdbi) { handle ->
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(handle)

            // when: storing a public channel owned by another user
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo2.id, Type.PUBLIC)
            repo.createChannel(channel)

            // and: searching for the channel by name
            val retrievedChannel: List<Channel> = repo.searchChannelsByName(channelName, null)
            val createdChannel = retrievedChannel[0]

            // when: asking if user is the owner of the channel
            val isOwner = repo.isOwnerChannel(createdChannel.id, testUserInfo.id)

            // then: response is false
            assertFalse(isOwner)

            // when: getting the channels owned by the user
            val ownedChannels = repo.getUserOwnedChannels(testUserInfo.id, null)

            // then: the channel is not in the list
            assertFalse(ownedChannels.contains(createdChannel))

            // and: the list is not empty
            assertFalse(ownedChannels.isEmpty())

            // when: asking if user2 is the owner of the channel
            val isOwner2 = repo.isOwnerChannel(createdChannel.id, testUserInfo2.id)

            // then: response is true
            assertTrue(isOwner2)

            // when: getting the channels owned by the user2
            val ownedChannels2 = repo.getUserOwnedChannels(testUserInfo2.id, null)

            // then: the channel is in the list
            assertTrue(ownedChannels2.contains(createdChannel))
        }
    }

    @Test
    fun `get public channels`() {
        runWithHandle(jdbi) { handle ->
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(handle)

            // when: storing a public channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PUBLIC)
            val channelId = repo.createChannel(channel)

            // when: storing a private channel
            val channelName2 = newTestChannelName()
            val channel2 = ChannelModel(channelName2, testUserInfo.id, Type.PRIVATE)
            val channelId2 = repo.createChannel(channel2)

            // and: searching for the channels by id
            val retrievedChannel: Channel? = repo.getChannelById(channelId)
            val retrievedChannel2: Channel? = repo.getChannelById(channelId2)

            // when: getting the public channels
            val publicChannels = repo.getPublicChannels(null)

            // then: the channel is in the list
            assertTrue(publicChannels.contains(retrievedChannel))

            // and: the private channel is not in the list
            assertFalse(publicChannels.contains(retrievedChannel2))
        }
    }

    @Test
    fun `updating channel name`() {
        runWithHandle(jdbi) { handle ->
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(handle)

            // and: a public channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PUBLIC)
            repo.createChannel(channel)

            // and: getting the channel by id
            val retrievedChannel: List<Channel> = repo.searchChannelsByName(channelName, null)
            val createdChannel = retrievedChannel[0]

            // when: updating the channel name
            val newChannelName = newTestChannelName()
            repo.updateChannelName(createdChannel.id, newChannelName)

            // and: searching for the channel by name
            val updatedChannel: List<Channel> = repo.searchChannelsByName(newChannelName, null)

            // then: the channel is found
            assertTrue(updatedChannel.size == 1)
            val updated = updatedChannel[0]
            assertEquals(newChannelName, updated.name)
            assertEquals(createdChannel.owner, updated.owner)
            assertEquals(createdChannel.id, updated.id)
        }
    }

    @Test
    fun `user join public channel`() {
        runWithHandle(jdbi) {
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(it)

            // and: a public channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PUBLIC)
            val channelId = repo.createChannel(channel)

            // when: user joins the channel
            repo.joinChannel(testUserInfo2.id, channelId)

            // and: getting the channel by id
            val retrievedChannel: Channel? = repo.getChannelById(channelId)

            // then:
            assertNotNull(retrievedChannel)
            assertTrue(retrievedChannel.members.size == 2)
            assertTrue(retrievedChannel.members.contains(testUserInfo))
            assertTrue(retrievedChannel.members.contains(testUserInfo2))
        }
    }

    @Test
    fun `user leave public channel`() {
        runWithHandle(jdbi) {
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(it)

            // and: a public channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PUBLIC)
            val channelId = repo.createChannel(channel)

            // and: user joins the channel
            repo.joinChannel(testUserInfo2.id, channelId)

            // when: user leaves the channel
            repo.leaveChannel(testUserInfo2.id, channelId)

            // and: getting the channel by id
            val retrievedChannel: Channel? = repo.getChannelById(channelId)

            // then:
            assertNotNull(retrievedChannel)
            assertTrue(retrievedChannel.members.size == 1)
            assertTrue(retrievedChannel.members.contains(testUserInfo))
            assertFalse(retrievedChannel.members.contains(testUserInfo2))
        }
    }

    @Test
    fun `user join a private channel`() {
        runWithHandle(jdbi) {
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(it)

            // and: a private channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PRIVATE)
            val channelId = repo.createChannel(channel)

            // when: creating an invitation for the channel
            val code = channelsDomain.generateInvitation(channelId)
            val inviteId = repo.createPrivateInvite(code, false)

            // and: sending the invite to the user
            val privacy = 1
            repo.sendInvitePrivateChannel(testUserInfo2.id, channelId, inviteId, privacy)

            // and: user joins the channel
            repo.joinMemberInChannelPrivate(testUserInfo2.id, channelId, code)

            // and: getting the channel by id
            val retrievedChannel: Channel? = repo.getChannelById(channelId)

            // then:
            assertNotNull(retrievedChannel)
            assertTrue(retrievedChannel.members.size == 2)
            assertTrue(retrievedChannel.members.contains(testUserInfo))
            assertTrue(retrievedChannel.members.contains(testUserInfo2))
        }
    }

    @Test
    fun `create a private channel READ_WRITE invite`() {
        runWithHandle(jdbi) {
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(it)

            // and: a private channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PRIVATE)
            val channelId = repo.createChannel(channel)

            // when: creating an invitation for the channel
            val code = channelsDomain.generateInvitation(channelId)
            val inviteId = repo.createPrivateInvite(code, false)

            // and: sending the invite to the user
            val privacy = 1
            val inviteSent = repo.sendInvitePrivateChannel(testUserInfo2.id, channelId, inviteId, privacy)

            // then: the invite is sent
            assertTrue(inviteSent > 0)

            // when: getting the type of the invite
            val inviteType = repo.getTypeInvitePrivateChannel(testUserInfo2.id, channelId)

            // then: the invite type is correct
            assertNotNull(inviteType)
            assertEquals(inviteType, Privacy.READ_WRITE)

            // when: checking if the invite code is valid
            val validInvite = repo.isPrivateChannelInviteCodeValid(testUserInfo2.id, channelId, code, false)

            // then: the invite is valid
            assertNotNull(validInvite)
            assertEquals(validInvite.id, channelId)
        }
    }

    @Test
    fun `create a private channel READ_ONLY invite`() {
        runWithHandle(jdbi) {
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(it)

            // and: a private channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PRIVATE)
            val channelId = repo.createChannel(channel)

            // when: creating an invitation for the channel
            val code = channelsDomain.generateInvitation(channelId)
            val inviteId = repo.createPrivateInvite(code, false)

            // and: sending the invite to the user
            val privacy = 0
            val inviteSent = repo.sendInvitePrivateChannel(testUserInfo2.id, channelId, inviteId, privacy)

            // then: the invite is sent
            assertTrue(inviteSent > 0)

            // when: getting the type of the invite
            val inviteType = repo.getTypeInvitePrivateChannel(testUserInfo2.id, channelId)

            // then: the invite type is correct
            assertNotNull(inviteType)
            assertEquals(inviteType, Privacy.READ_ONLY)

            // when: checking if the invite code is valid
            val validInvite = repo.isPrivateChannelInviteCodeValid(testUserInfo2.id, channelId, code, false)

            // then: the invite is valid
            assertNotNull(validInvite)
            assertEquals(validInvite.id, channelId)
        }
    }

    @Test
    fun `user leaves private channel`() {
        runWithHandle(jdbi) {
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(it)

            // and: a private channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PRIVATE)
            val channelId = repo.createChannel(channel)

            // and: creating an invitation for the channel
            val code = channelsDomain.generateInvitation(channelId)
            val inviteId = repo.createPrivateInvite(code, false)

            // and: sending the invite to the user
            val privacy = 0
            repo.sendInvitePrivateChannel(testUserInfo2.id, channelId, inviteId, privacy)

            // and: user joins the channel
            repo.joinMemberInChannelPrivate(testUserInfo2.id, channelId, code)

            // when: user leaves the channel
            repo.leaveChannel(testUserInfo2.id, channelId)

            // and: getting the channel by id
            val retrievedChannel: Channel? = repo.getChannelById(channelId)

            // then:
            assertNotNull(retrievedChannel)
            assertTrue(retrievedChannel.members.size == 1)
            assertTrue(retrievedChannel.members.contains(testUserInfo))
            assertFalse(retrievedChannel.members.contains(testUserInfo2))
        }
    }
}
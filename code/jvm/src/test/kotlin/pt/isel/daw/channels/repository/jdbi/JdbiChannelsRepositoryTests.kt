package pt.isel.daw.channels.repository.jdbi

import org.junit.jupiter.api.Test
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.channels.State
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.http.model.channel.ChannelUpdateInputModel
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
            repo.updateChannel(createdChannel.id, ChannelUpdateInputModel(newChannelName))

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
    fun `update channel type`() {
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

            // when: updating the channel type
            repo.updateChannel(createdChannel.id, ChannelUpdateInputModel(type = Type.PRIVATE))

            // and: searching for the channel by name
            val updatedChannel: List<Channel> = repo.searchChannelsByName(channelName, null)

            // then: the channel is found
            assertTrue(updatedChannel.size == 1)
            val updated = updatedChannel[0]
            assertEquals(channelName, updated.name)
            assertEquals(createdChannel.owner, updated.owner)
            assertEquals(createdChannel.id, updated.id)
            assertEquals(Type.PRIVATE, updated.type)
        }
    }

    @Test
    fun `user joins public channel`() {
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
            repo.createPrivateInvite(
                code,
                Privacy.READ_WRITE.ordinal,
                testUserInfo.id,
                testUserInfo2.id,
                channelId
            )

            // when: checking if the invite code is valid
            val validInvite = repo.isInviteCodeValid(testUserInfo2.id, channelId, code)

            // then: the invite is valid
            assertTrue(validInvite)
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
            repo.createPrivateInvite(
                code,
                Privacy.READ_ONLY.ordinal,
                testUserInfo.id,
                testUserInfo2.id,
                channelId
            )

            // when: checking if the invite code is valid
            val validInvite = repo.isInviteCodeValid(testUserInfo2.id, channelId, code)

            // then: the invite is valid
            assertTrue(validInvite)
        }
    }

    @Test
    fun `user joins a private channel and gets READ_WRITE permissions`() {
        runWithHandle(jdbi) {
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(it)

            // and: a private channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PRIVATE)
            val channelId = repo.createChannel(channel)

            // when: creating an invitation for the channel
            val code = channelsDomain.generateInvitation(channelId)
            repo.createPrivateInvite(
                code,
                Privacy.READ_WRITE.ordinal,
                testUserInfo.id,
                testUserInfo2.id,
                channelId
            )

            // and: user joins the channel
            repo.channelInviteAccepted(testUserInfo2.id, channelId, code)

            // when: getting the type of the invite
            val inviteType = repo.getMemberPermissions(testUserInfo2.id, channelId)

            // then: the invite type is correct
            assertNotNull(inviteType)
            assertEquals(inviteType, Privacy.READ_WRITE)

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
    fun `user joins a private channel and gets READ_ONLY permissions`() {
        runWithHandle(jdbi) {
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(it)

            // and: a private channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PRIVATE)
            val channelId = repo.createChannel(channel)

            // when: creating an invitation for the channel
            val code = channelsDomain.generateInvitation(channelId)
            repo.createPrivateInvite(
                code,
                Privacy.READ_ONLY.ordinal,
                testUserInfo.id,
                testUserInfo2.id,
                channelId
            )

            // and: user joins the channel
            repo.channelInviteAccepted(testUserInfo2.id, channelId, code)

            // when: getting the type of the invite
            val inviteType = repo.getMemberPermissions(testUserInfo2.id, channelId)

            // then: the invite type is correct
            assertNotNull(inviteType)
            assertEquals(inviteType, Privacy.READ_ONLY)

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
    fun `user rejects private channel invite`() {
        runWithHandle(jdbi) {
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(it)

            // and: a private channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PRIVATE)
            val channelId = repo.createChannel(channel)

            // when: creating an invitation for the channel
            val code = channelsDomain.generateInvitation(channelId)
            repo.createPrivateInvite(
                code,
                Privacy.READ_ONLY.ordinal,
                testUserInfo.id,
                testUserInfo2.id,
                channelId
            )

            // and: user rejects the channel
            repo.channelInviteRejected(testUserInfo2.id, channelId, code)

            // when: getting the channel by id
            val retrievedChannel: Channel? = repo.getChannelById(channelId)

            // then:
            assertNotNull(retrievedChannel)
            assertTrue(retrievedChannel.members.size == 1)
            assertTrue(retrievedChannel.members.contains(testUserInfo))
            assertFalse(retrievedChannel.members.contains(testUserInfo2))
        }
    }

    @Test
    fun `ban and unban user from public channel`() {
        runWithHandle(jdbi) {
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(it)

            // and: a public channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PUBLIC)
            val channelId = repo.createChannel(channel)

            // and: user joins the channel
            repo.joinChannel(testUserInfo2.id, channelId)

            // when: banning the user from the channel
            repo.updateChannelUserState(testUserInfo2.id, channelId, State.BANNED)

            // and: getting the channel by id
            val retrievedChannel: Channel? = repo.getChannelById(channelId)

            // then:
            assertNotNull(retrievedChannel)
            assertTrue(retrievedChannel.members.size == 1)
            assertTrue(retrievedChannel.members.contains(testUserInfo))
            assertFalse(retrievedChannel.members.contains(testUserInfo2))
            assertFalse(retrievedChannel.bannedMembers.isEmpty())
            assertTrue(retrievedChannel.bannedMembers.contains(testUserInfo2))

            // when: unbanning the user from the channel
            repo.updateChannelUserState(testUserInfo2.id, channelId, State.UNBANNED)

            // and: getting the channel by id
            val retrievedChannel2: Channel? = repo.getChannelById(channelId)

            // then:
            assertNotNull(retrievedChannel2)
            assertTrue(retrievedChannel2.members.size == 2)
            assertTrue(retrievedChannel2.members.contains(testUserInfo))
            assertTrue(retrievedChannel2.members.contains(testUserInfo2))
            assertTrue(retrievedChannel2.bannedMembers.isEmpty())
        }
    }

    @Test
    fun `ban and unban user from private channel`() {
        runWithHandle(jdbi) {
            // given: a ChannelsRepository
            val repo = JdbiChannelsRepository(it)

            // and: a private channel
            val channelName = newTestChannelName()
            val channel = ChannelModel(channelName, testUserInfo.id, Type.PRIVATE)
            val channelId = repo.createChannel(channel)

            // and: creating an invitation for the channel
            val code = channelsDomain.generateInvitation(channelId)
            repo.createPrivateInvite(
                code,
                Privacy.READ_ONLY.ordinal,
                testUserInfo.id,
                testUserInfo2.id,
                channelId
            )

            // and: user joins the channel
            repo.channelInviteAccepted(testUserInfo2.id, channelId, code)

            // when: banning the user from the channel
            repo.updateChannelUserState(testUserInfo2.id, channelId, State.BANNED)

            // and: getting the channel by id
            val retrievedChannel: Channel? = repo.getChannelById(channelId)

            // then:
            assertNotNull(retrievedChannel)
            assertTrue(retrievedChannel.members.size == 1)
            assertTrue(retrievedChannel.members.contains(testUserInfo))
            assertFalse(retrievedChannel.members.contains(testUserInfo2))
            assertFalse(retrievedChannel.bannedMembers.isEmpty())
            assertTrue(retrievedChannel.bannedMembers.contains(testUserInfo2))

            // when: unbanning the user from the channel
            repo.updateChannelUserState(testUserInfo2.id, channelId, State.UNBANNED)

            // and: getting the channel by id
            val retrievedChannel2: Channel? = repo.getChannelById(channelId)

            // then:
            assertNotNull(retrievedChannel2)
            assertTrue(retrievedChannel2.members.size == 2)
            assertTrue(retrievedChannel2.members.contains(testUserInfo))
            assertTrue(retrievedChannel2.members.contains(testUserInfo2))
            assertTrue(retrievedChannel2.bannedMembers.isEmpty())
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
            repo.createPrivateInvite(
                code,
                Privacy.READ_ONLY.ordinal,
                testUserInfo.id,
                testUserInfo2.id,
                channelId
            )

            // and: user joins the channel
            repo.channelInviteAccepted(testUserInfo2.id, channelId, code)

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
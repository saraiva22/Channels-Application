package pt.isel.daw.channels.repository.jdbi

import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.runWithHandle

class JdbiMessagesRepositoryTests: RepositoryTests() {

    @Test
    fun `can create a message in a channel and gets channels messages`() {
        runWithHandle(jdbi) { handle ->
            // given: a ChannelsRepository and MessagesRepository
            val messagesRepo = JdbiMessageRepository(handle)
            val channelsRepo = JdbiChannelsRepository(handle)

            // when: storing a public channel
            val channelName = newTestChannelName()
            val channelModel = ChannelModel(channelName, testUserInfo.id, Type.PUBLIC)
            val channelId = channelsRepo.createChannel(channelModel)

            // and: creating a message in the channel
            val messageText = newMessageText()
            val messageId = messagesRepo.createMessage(channelId, testUserInfo.id, messageText, Instant.DISTANT_PAST)

            // then: the message id is greater than 0
            assertTrue(messageId > 0)

            // then: getting the channel messages
            val channelById = channelsRepo.getChannelById(channelId)
            assertNotNull(channelById)

            channelById?.let { channel ->
                val messagesList = messagesRepo.getChannelMessages(channel)
                assertTrue(messagesList.isNotEmpty())
                val message = messagesList.first()
                assertTrue(message.text == messageText)
                assertTrue(message.channel == channelById)
                assertTrue(message.user == testUserInfo)
            }
        }
    }

    @Test
    fun `delete a message from a channel`() {
        runWithHandle(jdbi) { handle ->
            // given: a ChannelsRepository and MessagesRepository
            val messagesRepo = JdbiMessageRepository(handle)
            val channelsRepo = JdbiChannelsRepository(handle)

            // when: storing a public channel
            val channelName = newTestChannelName()
            val channelModel = ChannelModel(channelName, testUserInfo.id, Type.PUBLIC)
            val channelId = channelsRepo.createChannel(channelModel)

            // and: creating a message in the channel
            val messageText = newMessageText()
            val messageId = messagesRepo.createMessage(channelId, testUserInfo.id, messageText, Instant.DISTANT_PAST)

            // then: the message id is greater than 0
            assertTrue(messageId > 0)

            // then: getting the channel messages
            val channelById = channelsRepo.getChannelById(channelId)
            assertNotNull(channelById)

            channelById?.let { channel ->
                val messagesList = messagesRepo.getChannelMessages(channel)
                assertTrue(messagesList.isNotEmpty())
                val message = messagesList.first()
                assertTrue(message.text == messageText)
                assertTrue(message.channel == channelById)
                assertTrue(message.user == testUserInfo)

                // when: deleting the message
                messagesRepo.deleteMessageFromChannel(message.id, channel.id)

                // then: the message is not in the channel messages
                val messagesListAfterDelete = messagesRepo.getChannelMessages(channel)
                assertTrue(messagesListAfterDelete.isEmpty())
            }
        }
    }
}
package pt.isel.daw.channels.services

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import pt.isel.daw.channels.domain.channels.ChannelModel
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.repository.jdbi.RepositoryTests.Companion.testUserInfo
import pt.isel.daw.channels.utils.Either
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class MessageServiceTests: ServiceTests() {

    @Test
    fun `can create a message in a channel and gets channels messages`() {
        // given: a channel service and a message service
        val messagesService = createMessageService()
        val channelsService = createChannelService()

        // when: storing a public channel
        val channelName = newTestChannelName()
        val channelModel = ChannelModel(channelName, testUser.id, Type.PUBLIC)
        val channelId = channelsService.createChannel(channelModel)

        // then: the channel creation is successful
        when (channelId) {
            is Either.Left -> fail("Unexpected $channelId")
            is Either.Right -> assertTrue(channelId.value > 0)
        }

        // and: creating a message in the channel
        val messageText = newMessageText()
        val messageId = messagesService.createMessage(channelId.value, testUser, messageText)

        // then: the message creation is successful
        when (messageId) {
            is Either.Left -> fail("Unexpected $messageId")
            is Either.Right -> assertTrue(messageId.value > 0)
        }

        // then: getting the channel messages
        val channelById = channelsService.getChannelById(testUser.id, channelId.value)
        assertNotNull(channelById)

        when (channelById) {
            is Either.Left -> fail("Unexpected $channelById")
            is Either.Right -> {
                val messagesList = messagesService.getChannelMessages(testUser.id, channelById.value.id)
                when (messagesList) {
                    is Either.Left -> fail("Unexpected $messagesList")
                    is Either.Right -> {
                        assertTrue(messagesList.value.isNotEmpty())
                        val message = messagesList.value.first()
                        assertEquals(message.text, messageText)
                        assertEquals(message.channel, channelById.value)
                        assertEquals(message.user, testUserInfo)
                    }
                }
            }
        }
    }

    @Test
    fun `delete a message from a channel`() {
        // given: a channel service and a message service
        val messagesService = createMessageService()
        val channelsService = createChannelService()

        // when: storing a public channel
        val channelName = newTestChannelName()
        val channelModel = ChannelModel(channelName, testUser.id, Type.PUBLIC)
        val channelId = channelsService.createChannel(channelModel)

        // then: the channel creation is successful
        when (channelId) {
            is Either.Left -> fail("Unexpected $channelId")
            is Either.Right -> assertTrue(channelId.value > 0)
        }

        // and: creating a message in the channel
        val messageText = newMessageText()
        val messageId = messagesService.createMessage(channelId.value, testUser, messageText)

        // then: the message creation is successful
        when (messageId) {
            is Either.Left -> fail("Unexpected $messageId")
            is Either.Right -> assertTrue(messageId.value > 0)
        }

        // then: getting the channel messages
        val channelById = channelsService.getChannelById(testUser.id, channelId.value)
        assertNotNull(channelById)

        when (channelById) {
            is Either.Left -> fail("Unexpected $channelById")
            is Either.Right -> {
                val messagesList = messagesService.getChannelMessages(testUser.id, channelById.value.id)
                when (messagesList) {
                    is Either.Left -> fail("Unexpected $messagesList")
                    is Either.Right -> {
                        assertTrue(messagesList.value.isNotEmpty())
                        val message = messagesList.value.first()
                        assertEquals(message.text, messageText)
                        assertEquals(message.channel, channelById.value)
                        assertEquals(message.user, testUserInfo)
                    }
                }
            }
        }

        // and: deleting the message
        val deleteMessageResult = messagesService.deleteMessageFromChannel(testUser.id, messageId.value, channelId.value)

        // then: the message deletion is successful
        when (deleteMessageResult) {
            is Either.Left -> fail("Unexpected $deleteMessageResult")
            is Either.Right -> assertTrue(deleteMessageResult.value)
        }

        // then: getting the channel messages
        val messagesList = messagesService.getChannelMessages(testUser.id, channelById.value.id)
        when (messagesList) {
            is Either.Left -> fail("Unexpected $messagesList")
            is Either.Right -> assertTrue(messagesList.value.isEmpty())
        }
    }
}
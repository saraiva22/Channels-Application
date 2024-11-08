package pt.isel.daw.channels.http

import org.junit.jupiter.api.Test
import pt.isel.daw.channels.clearData
import pt.isel.daw.channels.clearInvitationRegisterData
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.http.model.message.MessageListOutputModel
import pt.isel.daw.channels.http.model.utils.IdOutputModel
import kotlin.test.assertNotNull

class MessagesControllerTests: ControllerTests() {

    @Test
    fun `create a message in a channel and gets its messages`() {
        val code = generateInvitationCode()
        val user = createUser(password = password, code = code)
        val token = createToken(user.username, password)
        val channel = newTestChannelName()

        val channelId = createChannel(token, channel, Type.PUBLIC)

        val createMessage =
            client.post().uri(api("/channels/$channelId/messages"))
                .header("Authorization", "Bearer $token")
                .bodyValue(
                    mapOf(
                        "text" to "Hello World!"
                    )
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody(IdOutputModel::class.java)
                .returnResult()
                .responseBody?.id

        assertNotNull(createMessage)

        val channelMessages =
            client.get().uri(api("/channels/$channelId/messages"))
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(MessageListOutputModel::class.java)
                .returnResult()
                .responseBody

        assertNotNull(channelMessages)
        assertNotNull(
            channelMessages.find { outputModel ->
                outputModel.messages.any { message ->
                    message.id == createMessage && message.text == "Hello World!"
                }
            }
        )

        if (code != null) clearInvitationRegisterData(jdbi, code)
        clearData(jdbi, "dbo.Messages", "id", createMessage)
        clearData(jdbi, "dbo.Join_Channels", "ch_id", channelId)
        clearData(jdbi, "dbo.Channels", "id", channelId)
        clearData(jdbi, "dbo.Users", "id", user.id)
    }

    @Test
    fun `delete a message from a channel`() {
        val code = generateInvitationCode()
        val user = createUser(password = password, code = code)
        val token = createToken(user.username, password)
        val channel = newTestChannelName()

        val channelId = createChannel(token, channel, Type.PUBLIC)

        val createMessage =
            client.post().uri(api("/channels/$channelId/messages"))
                .header("Authorization", "Bearer $token")
                .bodyValue(
                    mapOf(
                        "text" to "Hello World!"
                    )
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody(IdOutputModel::class.java)
                .returnResult()
                .responseBody?.id

        assertNotNull(createMessage)

        client.delete().uri(api("/channels/$channelId/messages/$createMessage"))
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk

        client.delete().uri(api("/channels/$channelId/messages/$createMessage"))
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound

        clearData(jdbi, "dbo.Join_Channels", "ch_id", channelId)
        clearData(jdbi, "dbo.Channels", "id", channelId)
        clearData(jdbi, "dbo.Users", "id", user.id)
    }
}
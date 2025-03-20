package pt.isel.daw.channels.http

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import pt.isel.daw.channels.domain.channels.Status
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.http.model.TokenResponse
import pt.isel.daw.channels.http.model.UserInviteResponse
import pt.isel.daw.channels.http.model.channel.ChannelOutputModel
import pt.isel.daw.channels.http.model.channel.ChannelsListOutputModel
import pt.isel.daw.channels.http.model.channel.RegisterPrivateInviteOutputModel
import pt.isel.daw.channels.http.model.utils.IdOutputModel
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertFailsWith


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChannelsControllerTests {
    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0


    @Test
    fun `can create a channel`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and a random channel private
        val channelPrivateName = newTestChannelName()
        val privacyPrivate = private

        // and a random channel public
        val channelPublicName = newTestChannelName()
        val privacyPublic = public

        // when: getting the token
        // then: the response is a 200
        val result = getTokenUserAdmin(client)

        // when: creating a private channel
        // then: the response is a 201 with a proper Location header
        client.post().uri("/channels/create")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "name" to channelPrivateName,
                    "type" to privacyPrivate
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().value("location") {
                assertTrue(it.startsWith("/api/channels"))
            }

        // when: creating a channel with the same name
        // then: the response is a 400 with the proper problem
        client.post().uri("/channels/create")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "name" to channelPrivateName,
                    "privacy" to privacyPrivate
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType("application/problem+json")

        // when: creating a channel with an invalid token
        // then: the response is a 401 with the proper problem
        client.post().uri("/channels/create")
            .header("Authorization", "Bearer invalid")
            .bodyValue(
                mapOf(
                    "name" to channelPrivateName,
                    "privacy" to privacyPrivate
                ),
            )
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")

        // when: creating a channel with an invalid privacy
        // then: the response is a 400 with the proper problem
        client.post().uri("/channels/create")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "name" to channelPrivateName,
                    "privacy" to "invalid"
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType("application/problem+json")

        // when: creating a public channel
        // then: the response is a 201 with a proper Location header
        client.post().uri("/channels/create")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "name" to channelPublicName,
                    "type" to privacyPublic
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().value("location") {
                assertTrue(it.startsWith("/api/channels"))
            }


    }


    @Test
    fun `test private channel lifecycle with invite, join, leave, and rejoin attempts`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and a random channel private
        val channelPrivateName = newTestChannelName()
        val typePrivate = private
        val privacyReadWrite = READ_WRITE

        // when: creating a token
        // then: the response is a 200
        val result = getTokenUserAdmin(client)

        // when: creating an invite code
        // then: the response is a 201 with a proper Location header
        val invite = client.post().uri("/users/invite")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus().isCreated
            .expectBody(UserInviteResponse::class.java)
            .returnResult()
            .responseBody!!

        // when: creating a token new user
        // then: the response is a 200
        val resultNewUser = getTokenUserRandom(client)

        // when: creating a private channel
        // then: the response is a 201 with a proper Location header
        val channelId = client.post().uri("/channels/create")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "name" to channelPrivateName,
                    "type" to typePrivate
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(IdOutputModel::class.java)
            .returnResult()
            .responseBody!!.id


        // when: create invite to private channel
        // then: the response is a 201 with a proper Location header
        val inviteResponse = client.post().uri("/channels/${channelId}/private-invite")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "privacy" to privacyReadWrite,
                    "username" to USERNAME_TEST1
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(RegisterPrivateInviteOutputModel::class.java)
            .returnResult()
            .responseBody!!

        val code = inviteResponse.inviteLink.split("/").last()

        // when: join in private channel
        // then: the response is a 200 with a proper Location header
        val channel = client.post().uri("/channels/${channelId}/invite/${code}")
            .header("Authorization", "Bearer ${resultNewUser.token}")
            .bodyValue(
                mapOf(
                    "status" to Status.ACCEPT
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(ChannelOutputModel::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(channelId, channel.id)
        assertEquals(channelPrivateName, channel.name)
        assertEquals(USERNAME_TEST, channel.owner.username)
        assertEquals(2, channel.members.size)
        assertEquals(USERNAME_TEST1, channel.members[1].username)
        assertEquals(USERNAME_TEST, channel.members[0].username)

        // when: leave a channel
        // then: the response is a 200
        client.post().uri("/channels/${channelId}/leave")
            .header("Authorization", "Bearer ${resultNewUser.token}")
            .exchange()
            .expectStatus().isOk


        // when: try join a channel
        // then: the response is a 400
        client.post().uri("/channels/${channelId}/invite/${code}")
            .header("Authorization", "Bearer ${resultNewUser.token}")
            .bodyValue(
                mapOf(
                    "status" to Status.ACCEPT
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType("application/problem+json")
    }

    @Test
    fun `test private channel with reject invite`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and a random channel private
        val channelPrivateName = newTestChannelName()
        val typePrivate = private
        val privacyReadWrite = READ_WRITE

        // when: creating a token
        // then: the response is a 200
        val result = getTokenUserAdmin(client)

        // when: creating an invite code
        // then: the response is a 201 with a proper Location header
        val invite = client.post().uri("/users/invite")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus().isCreated
            .expectBody(UserInviteResponse::class.java)
            .returnResult()
            .responseBody!!

        // when: creating a token new user
        // then: the response is a 200
        val resultNewUser = getTokenUserRandom(client)

        // when: creating a private channel
        // then: the response is a 201 with a proper Location header
        val channelId = client.post().uri("/channels/create")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "name" to channelPrivateName,
                    "type" to typePrivate
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(IdOutputModel::class.java)
            .returnResult()
            .responseBody!!.id


        // when: create invite to private channel
        // then: the response is a 201 with a proper Location header
        val inviteResponse = client.post().uri("/channels/${channelId}/private-invite")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "privacy" to privacyReadWrite,
                    "username" to USERNAME_TEST1
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(RegisterPrivateInviteOutputModel::class.java)
            .returnResult()
            .responseBody!!

        val code = inviteResponse.inviteLink.split("/").last()

        // when: reject invite to private channel
        // then: the response is a 200 with a proper Location header
        client.post().uri("/channels/${channelId}/invite/${code}")
            .header("Authorization", "Bearer ${resultNewUser.token}")
            .bodyValue(
                mapOf(
                    "status" to Status.REJECT
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(ChannelOutputModel::class.java)
            .returnResult()

        val channel = client.get().uri("/channels/${channelId}")
            .header("Authorization", "Bearer ${resultNewUser.token}")
            .exchange()
            .expectStatus().isForbidden
            .expectHeader().contentType("application/problem+json")



        // when: try join a channel
        // then: the response is a 400
        client.post().uri("/channels/${channelId}/invite/${code}")
            .header("Authorization", "Bearer ${resultNewUser.token}")
            .bodyValue(
                mapOf(
                    "status" to Status.ACCEPT
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType("application/problem+json")
    }

    @Test
    fun `create public channel, join, leave, and retrieval`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and a random channel public
        val channelPublicName = newTestChannelName()
        val privacyPublic = public

        // when: getting the token
        // then: the response is a 200
        val result = getTokenUserAdmin(client)

        // when: creating a public channel
        // then: the response is a 201 with a proper Location header
        val channelId = client.post().uri("/channels/create")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "name" to channelPublicName,
                    "type" to privacyPublic
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(IdOutputModel::class.java)
            .returnResult()
            .responseBody!!.id

        // when: getting channel public
        // then: the response is a 200
        val res = client.get().uri("/channels/public")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus().isOk
            .expectBody(ChannelsListOutputModel::class.java)
            .returnResult()
            .responseBody!!.channels


        val channel = res.first{it.id ==channelId}

        assertEquals(channelId, channel.id)
        assertEquals(channelPublicName, channel.name)
        assertEquals(USERNAME_TEST, channel.owner.username)
        assertEquals(1, channel.members.size)


        // when getting token user random
        // then: the response is a 200
        val resultUserRandom = getTokenUserRandom(client)

        // when: join in public channel
        // then: the response is a 200 with a proper Location header
        val channelJoin = client.post().uri("/channels/${channelId}")
            .header("Authorization", "Bearer ${resultUserRandom.token}")
            .exchange()
            .expectStatus().isOk
            .expectBody(ChannelOutputModel::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(channelId, channelJoin.id)
        assertEquals(channelPublicName, channelJoin.name)
        assertEquals(USERNAME_TEST, channelJoin.owner.username)
        assertEquals(2, channelJoin.members.size)
        assertEquals(USERNAME_TEST1, channelJoin.members[1].username)
        assertEquals(USERNAME_TEST, channelJoin.members[0].username)

        //when: leave user random channel
        //then: the response is a 200
        client.post().uri("/channels/${channelId}/leave")
            .header("Authorization", "Bearer ${resultUserRandom.token}")
            .exchange()
            .expectStatus().isOk

        // when: get channel public id
        // then: the response is a 200
        client.get().uri("/channels/${channelId}")
            .header("Authorization", "Bearer ${resultUserRandom.token}")
            .exchange()
            .expectStatus().isForbidden
            .expectHeader().contentType("application/problem+json")


        // when: get public
        // then: the response is a 200
        val res2 = client.get().uri("/channels/public")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus().isOk
            .expectBody(ChannelsListOutputModel::class.java)
            .returnResult()
            .responseBody!!.channels

        val channel2 = res2.first{it.id ==channelId}
        assertEquals(channelId, channel2.id)
        assertEquals(channelPublicName, channel2.name)
        assertEquals(USERNAME_TEST, channel2.owner.username)
        assertEquals(1, channel2.members.size)

    }

    @Test
    fun `ban and unban user from public channel`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and a random channel public
        val channelPublicName = newTestChannelName()
        val privacyPublic = public

        // when: getting the token
        // then: the response is a 200
        val result = getTokenUserAdmin(client)

        // when: creating a public channel
        // then: the response is a 201 with a proper Location header
        val channelId = client.post().uri("/channels/create")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "name" to channelPublicName,
                    "type" to privacyPublic
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(IdOutputModel::class.java)
            .returnResult()
            .responseBody!!.id

        // when getting token user random
        // then: the response is a 200
        val resultUserRandom = getTokenUserRandom(client)

        // when: join in public channel
        // then: the response is a 200 with a proper Location header
        val channelJoin = client.post().uri("/channels/${channelId}")
            .header("Authorization", "Bearer ${resultUserRandom.token}")
            .exchange()
            .expectStatus().isOk
            .expectBody(ChannelOutputModel::class.java)
            .returnResult()
            .responseBody!!

        // when: ban user random
        // then: the response is a 200
        val channelWithBannedMembers = client.post().uri("/channels/${channelId}/ban")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "username" to USERNAME_TEST1
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(ChannelOutputModel::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(channelId, channelWithBannedMembers.id)
        assertEquals(channelPublicName, channelWithBannedMembers.name)
        assertEquals(USERNAME_TEST, channelWithBannedMembers.owner.username)
        assertEquals(1, channelWithBannedMembers.members.size)
        assertEquals(1, channelWithBannedMembers.bannedMembers.size)
        assertEquals(USERNAME_TEST1, channelWithBannedMembers.bannedMembers[0].username)

        // when: unban user random
        // then: the response is a 200
        val channelAfterUnban = client.post().uri("/channels/${channelId}/unban")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "username" to USERNAME_TEST1
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(ChannelOutputModel::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(channelId, channelAfterUnban.id)
        assertEquals(channelPublicName, channelAfterUnban.name)
        assertEquals(USERNAME_TEST, channelAfterUnban.owner.username)
        assertEquals(2, channelAfterUnban.members.size)
        assertEquals(0, channelAfterUnban.bannedMembers.size)
    }


    companion object {
        private fun newTestChannelName() = "channel-${abs(Random.nextInt())}"
        private fun newTestUsername() = "user-${abs(Random.nextInt())}"
        private fun newTestEmail() = "user-${abs(Random.nextInt())}@mail.com"
        private val private = Type.PRIVATE
        private val public = Type.PUBLIC
        private const val READ_WRITE = "READ_WRITE"

        // and: user exist in the database
        private const val USERNAME_TEST = "Test99"
        private const val PASSWORD_TEST = "Test_999"

        // and random user
        private const val USERNAME_TEST1 = "random"
        private const val PASSWORD_TEST1 = "Random@123"
        private const val EMAIL_TEST1 = "random1@gmail.com"

        private fun getTokenUserRandom(client: WebTestClient): TokenResponse {
            return client.post().uri("/users/token")
                .bodyValue(
                    mapOf(
                        "username" to USERNAME_TEST1,
                        "password" to PASSWORD_TEST1,
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody(TokenResponse::class.java)
                .returnResult()
                .responseBody!!
        }


        private fun getTokenUserAdmin(client: WebTestClient): TokenResponse {
            return client.post().uri("/users/token")
                .bodyValue(
                    mapOf(
                        "username" to USERNAME_TEST,
                        "password" to PASSWORD_TEST,
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody(TokenResponse::class.java)
                .returnResult()
                .responseBody!!
        }
    }
}
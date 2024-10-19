package pt.isel.daw.channels.http

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.http.model.ChannelInviteResponse
import pt.isel.daw.channels.http.model.TokenResponse
import pt.isel.daw.channels.http.model.UserInviteResponse
import pt.isel.daw.channels.http.model.channel.ChannelOutputModel
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test


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
        val result =
            client.post().uri("/users/token")
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

        // and user random
        val username = newTestUsername()
        val email = newTestEmail()
        val password = "changeit"

        // and a random channel private
        val channelPrivateName = newTestChannelName()
        val typePrivate = private
        val privacyReadWrite = READ_WRITE

        // when: creating a token
        // then: the response is a 200
        val result =
            client.post().uri("/users/token")
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

        // when: creating an invite code
        // then: the response is a 201 with a proper Location header
        val invite = client.post().uri("/users/invite")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus().isCreated
            .expectBody(UserInviteResponse::class.java)
            .returnResult()
            .responseBody!!

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to username,
                    "password" to password,
                    "email" to email,
                    "inviteCode" to invite.code
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().value("location") {
                kotlin.test.assertTrue(it.startsWith("/api/users/"))
            }

        // when: creating a token new user
        // then: the response is a 200
        val resultNewUser =
            client.post().uri("/users/token")
                .bodyValue(
                    mapOf(
                        "username" to username,
                        "password" to password,
                    ),
                )
                .exchange()
                .expectStatus().isOk
                .expectBody(TokenResponse::class.java)
                .returnResult()
                .responseBody!!


        // when: creating a private channel
        // then: the response is a 201 with a proper Location header
        val response = client.post().uri("/channels/create")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "name" to channelPrivateName,
                    "type" to typePrivate
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().value("location") {
                assertTrue(it.startsWith("/api/channels"))
            }.returnResult<Unit>()


        val channelId = response.responseHeaders["Location"]?.get(0)?.split("/")?.last()?.toInt()!!


        // when: create invite to private channel
        // then: the response is a 201 with a proper Location header
        val inviteResponse = client.post().uri("/channels/${channelId}/private-invite")
            .header("Authorization", "Bearer ${result.token}")
            .bodyValue(
                mapOf(
                    "privacy" to privacyReadWrite,
                    "username" to username
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(ChannelInviteResponse::class.java)
            .returnResult()
            .responseBody!!



        // when: join in private channel
        // then: the response is a 201 with a proper Location header
        val channel = client.post().uri("/channels/${channelId}/private")
            .header("Authorization", "Bearer ${resultNewUser.token}")
            .bodyValue(
                mapOf(
                    "codHash" to inviteResponse.codHash
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
        assertEquals(username, channel.members[1].username)

        // when: leave a channel
        // then: the response is a 200
        client.post().uri("/channels/${channelId}/leave")
            .header("Authorization", "Bearer ${resultNewUser.token}")
            .exchange()
            .expectStatus().isOk


        // when: try join a channel
        // then: the response is a 400
        client.post().uri("/channels/${channelId}/private")
            .header("Authorization", "Bearer ${resultNewUser.token}")
            .bodyValue(
                mapOf(
                    "inviteCode" to inviteResponse.codHash
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType("application/problem+json")



    }


    companion object {
        private fun newTestChannelName() = "channel-${abs(Random.nextInt())}"
        private fun newTestUsername() = "user-${abs(Random.nextInt())}"
        private fun newTestEmail() = "user-${abs(Random.nextInt())}@mail.com"
        private val private = Type.PRIVATE
        private val public = Type.PUBLIC
        private const val READ_WRITE = "READ_WRITE"

        // and: user exist in the database
        private const val USERNAME_TEST = "admin"
        private const val PASSWORD_TEST = "admin"
    }
}
package pt.isel.daw.channels.http

import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.http.model.TokenResponse
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


    companion object {
        private fun newTestChannelName() = "channel-${abs(Random.nextInt())}"
        private val private = Type.PRIVATE
        private val public = Type.PUBLIC

        // and: user exist in the database
        private const val USERNAME_TEST = "test"
        private const val PASSWORD_TEST = "12345"
    }
}
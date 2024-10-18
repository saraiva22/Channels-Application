package pt.isel.daw.channels.http

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import pt.isel.daw.channels.http.model.TokenResponse
import pt.isel.daw.channels.http.model.UserInviteResponse
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsersControllerTests {
    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `obtain a token, obtain a invite code, create an user, and access user home, and logout`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()


        // and: a random user
        val username = newTestUserName()
        val email = newTestEmail()
        val password = "changeit"

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

        // when: getting the token with a invalid user
        // then: the response is a 400 with the proper problem
        client.post().uri("/users/token")
            .bodyValue(
                mapOf(
                    "username" to username,
                    "password" to password,
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType("application/problem+json")

        // when: creating an invite code
        // then: the response is a 201 with a proper Location header
        val invite = client.post().uri("/users/invite")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus().isCreated
            .expectBody(UserInviteResponse::class.java)
            .returnResult()
            .responseBody!!

        // when: creating an invite code with an invalid token
        // then: the response is a 401 with the proper problem
        client.post().uri("/users/invite")
            .header("Authorization", "Bearer invalid")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")


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
                assertTrue(it.startsWith("/api/users/"))
            }

        //when: creating an user with an invalid invite code
        // then: the response is a 400 with the proper problem
        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to username,
                    "password" to password,
                    "email" to email,
                    "inviteCode" to "invalid"
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType("application/problem+json")

        // when: creating a token for the new user
        // then: the response is a 200
        val result2 =
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

        // when: getting the user home with a valid token
        // then: the response is a 200 with the proper representation
        client.get().uri("/me")
            .header("Authorization", "Bearer ${result2.token}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("username").isEqualTo(username)

        // when: getting the user home with an invalid token
        // then: the response is a 401 with the proper problem
        client.get().uri("/me")
            .header("Authorization", "Bearer ${result2.token}-invalid")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")

        // when: revoking the token
        // then: response is a 200
        client.post().uri("/logout")
            .header("Authorization", "Bearer ${result2.token}")
            .exchange()
            .expectStatus().isOk

        // when: getting the user home with the revoked token
        // then: response is a 401
        client.get().uri("/me")
            .header("Authorization", "Bearer ${result2.token}")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")
    }



    companion object {
        private fun newTestUserName() = "user-${abs(Random.nextLong())}"
        private fun newTestEmail() = "email-${abs(Random.nextLong())}@example.com"

        // and: user exist in the database
        private const val USERNAME_TEST = "test"
        private const val PASSWORD_TEST = "12345"
    }
}
package pt.isel.daw.channels.http

import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import pt.isel.daw.channels.ApplicationTests
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.http.model.channel.ChannelOutputModel
import pt.isel.daw.channels.http.model.user.UserHomeOutputModel
import pt.isel.daw.channels.http.model.user.UserTokenCreateOutputModel
import pt.isel.daw.channels.http.model.utils.IdOutputModel
import pt.isel.daw.channels.services.ServiceTests

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControllerTests: ServiceTests() {
    @LocalServerPort
    var port: Int = 0
    final fun api(path: String): String = "http://localhost:$port/api$path"
    final val client = WebTestClient.bindToServer().baseUrl(api("/")).build()

    fun createUser(
        username: String = newTestUserName(),
        email: String = newTestEmail(username),
        password: String,
        code: String? = null
    ): UserHomeOutputModel {
        val id = client.post().uri(api("/users"))
            .bodyValue(
                mapOf(
                    "username" to username,
                    "email" to email,
                    "password" to password,
                    "code" to code
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().value("location") {
                assertTrue(it.startsWith("/api/users"))
            }
            .expectBody(IdOutputModel::class.java)
            .returnResult()
            .responseBody!!.id

        val token = createToken(username, password)

        return getUserById(id, token)
    }

    fun createToken(username: String, password: String): String =
        client.post().uri(api("/users/token"))
            .bodyValue(
                mapOf(
                    "username" to username,
                    "password" to password
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(UserTokenCreateOutputModel::class.java)
            .returnResult()
            .responseBody!!.token

    fun getUserById(id: Int, token: String): UserHomeOutputModel =
        client.get().uri(api("/users/$id"))
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(UserHomeOutputModel::class.java)
            .returnResult()
            .responseBody!!

    fun createChannel(token: String, channelName: String, type: Type): Int =
        client.post().uri(api("/channels/create"))
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "name" to channelName,
                    "type" to type.name.uppercase()
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(IdOutputModel::class.java)
            .returnResult()
            .responseBody!!.id

    fun getChannelById(id: Int, token: String): ChannelOutputModel =
        client.get().uri(api("/channels/$id"))
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(ChannelOutputModel::class.java)
            .returnResult()
            .responseBody!!

    companion object {
        val password = "password"

        val private = Type.PRIVATE

        val public = Type.PUBLIC

        const val READ_WRITE = "READ_WRITE"
    }
}
package pt.isel.daw.channels.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

class Problem(
    typeUri: URI
) {
    val type = typeUri.toASCIIString()

    companion object {
        const val MEDIA_TYPE = "application/problem+json"

        fun response(status: Int, problem: Problem) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problem)

        val channelAlreadyExists = Problem(
            URI(
                "https://github.com/isel-leic-daw/2024-daw-leic51d-g10-1/blob/main/docs/problems/channel-already-exists.md",
            ),
        )

        val invalidChannelType = Problem(
            URI(
                "https://github.com/isel-leic-daw/2024-daw-leic51d-g10-1/blob/main/docs/problems/invalid-channel-type.md"
            )
        )
    }
}
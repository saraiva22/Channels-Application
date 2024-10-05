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
                "https://github.com/isel-leic-daw/s2223i-51d-51n-public/tree/main/code/tic-tac-tow-service/" +
                        "docs/problems/user-already-exists",
            ),
        )
    }
}
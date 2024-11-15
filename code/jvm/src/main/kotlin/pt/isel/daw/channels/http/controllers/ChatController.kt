package pt.isel.daw.channels.http.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.daw.channels.domain.user.AuthenticatedUser
import pt.isel.daw.channels.http.Uris
import pt.isel.daw.channels.http.util.SseEmitterBasedEventEmitter
import pt.isel.daw.channels.services.message.ChatService
import java.util.concurrent.TimeUnit

@RestController
class ChatController(
    private val chatService: ChatService
) {
    @GetMapping(Uris.Users.NOTIFICATIONS)
    fun notification(
        auth: AuthenticatedUser,
    ): SseEmitter {
        val sseEmitter = SseEmitter(TimeUnit.HOURS.toMillis(1))
        chatService.addEventEmitter(
            auth.user.id, auth.token,
            SseEmitterBasedEventEmitter(
                sseEmitter
            )
        )
        return sseEmitter
    }
}
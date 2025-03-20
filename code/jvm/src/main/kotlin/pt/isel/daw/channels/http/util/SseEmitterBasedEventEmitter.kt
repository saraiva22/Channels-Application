package pt.isel.daw.channels.http.util


// - SseEmitter - Spring MVC type
// - EventEmitter is our own type (domain)
// - SseEmitterBasedEventEmitter is our own type (http), which uses SseEmitter

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.daw.channels.domain.sse.Event
import pt.isel.daw.channels.domain.sse.EventEmitter

class SseEmitterBasedEventEmitter(
    private val sseEmitter: SseEmitter
) : EventEmitter {
    override fun emit(event: Event) {
        val event = when (event) {
            is Event.Message -> SseEmitter.event()
                .id(event.id.toString())
                .name("message")
                .data(event)
            is Event.Invite -> SseEmitter.event()
                .id(event.id.toString())
                .name("invite")
                .data(event)

            is Event.KeepAlive -> SseEmitter.event()
                .comment(event.timestamp.epochSeconds.toString())
        }
        sseEmitter.send(event)
    }

    override fun onCompletion(callback: () -> Unit) {
        sseEmitter.onCompletion(callback)
    }

    override fun onError(callback: (Throwable) -> Unit) {
        sseEmitter.onError(callback)
    }

    override fun complete() {
        sseEmitter.complete()
    }
}
package pt.isel.daw.channels.domain.sse

interface EventEmitter {
    fun emit(event: Event)

    fun onCompletion(callback: () -> Unit)

    fun onError(callback: (Throwable) -> Unit)

    fun complete()
}
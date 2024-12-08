package pt.isel.daw.channels.services.message

import jakarta.inject.Named
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.sse.Event
import pt.isel.daw.channels.domain.sse.EventEmitter
import pt.isel.daw.channels.domain.user.UserInfo
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Named
class ChatService : NeedsShutdown {
    // Important: mutable state on a singleton service
    // private val listeners = mutableListOf<EventEmitter>()
    // Map<UserId,List<Pair<TokenId,EventEmitter>>>()
    private val userListeners = mutableMapOf<Int, MutableList<Pair<String, EventEmitter>>>()
    private var currentId = 0L
    private val lock = ReentrantLock()

    // A scheduler to send the periodic keep-alive events
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1).also {
        it.scheduleAtFixedRate({ keepAlive() }, 2, 2, TimeUnit.SECONDS)
    }

    override fun shutdown() {
        logger.info("shutting down")
        scheduler.shutdown()
    }

    fun addEventEmitter(user: Int, token: String, listener: EventEmitter) = lock.withLock {
        logger.info("adding listener")
        val element = Pair(token, listener)
        userListeners.computeIfAbsent(user) { mutableListOf() }.add(element)
        listener.onCompletion {
            logger.info("onCompletion")
            removeListener(user, token)
        }
        listener.onError {
            logger.info("onError")
            removeListener(user, token)
        }
        listener
    }

    fun sendMessage(
        messageId: Int,
        text: String,
        channel: Channel,
        userInfo: UserInfo,
        created: String
    ) =
        lock.withLock {
            logger.info("sendMessage")
            val id = currentId++
            val membersChannel = channel.members.map { it.id }
            sendEventToAll(membersChannel, Event.Message(id, messageId, text,channel, userInfo, created))
        }


    fun disconnectListener(userId: Int, token: String) = removeListener(userId, token)


    private fun removeListener(userId: Int, token: String) = lock.withLock {
        logger.info("removing listener")
        userListeners[userId]?.let { list ->
            val (toRemove, newList) = list.partition { it.first == token }
            if (newList.isEmpty()) {
                userListeners.remove(userId)
            } else {
                userListeners[userId] = newList.toMutableList()
            }
            toRemove.firstOrNull()?.second?.complete()
        }
    }


    private fun keepAlive() = lock.withLock {
        logger.info(
            "keepAlive, sending to {} users and {} listeners",
            userListeners.count(),
            userListeners.values.flatten().count()
        )
        sendEventToAll(userListeners.map { it.key }, Event.KeepAlive(Clock.System.now()))
    }

    private fun sendEventToAll(membersChannel: List<Int>, event: Event) {
        membersChannel.forEach { member ->
            userListeners[member]?.let {
                try {
                    it.forEach { ev ->
                        ev.second.emit(event)
                    }
                } catch (ex: Exception) {
                    logger.info("Exception while sending event - {}", ex.message)
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ChatService::class.java)
    }
}
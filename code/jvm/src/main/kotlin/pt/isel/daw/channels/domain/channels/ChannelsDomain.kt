package pt.isel.daw.channels.domain.channels

import org.springframework.stereotype.Component
import java.util.*

@Component
class ChannelsDomain {
    
    fun generateInvitation(): String {
        val part1 = UUID.randomUUID().toString().take(5)
        val part2 = UUID.randomUUID().toString().take(5)
        return "$part1-$part2"
    }

    fun isUserMember(userId: Int, channel: Channel) = channel.members.contains(userId)
}
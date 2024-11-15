package pt.isel.daw.channels.domain.channels

import org.springframework.stereotype.Component
import org.springframework.web.util.UriTemplate
import pt.isel.daw.channels.http.Uris
import java.util.*


/**
 * Domain class for Channels
 */
@Component
class ChannelsDomain {

    fun generateInvitation(id: Int): String {
        val code = UUID.randomUUID().toString()
        return UriTemplate(Uris.Channels.JOIN_PRIVATE_CHANNELS).expand(id, code).toString()
    }

    fun isUserMember(userId: Int, channel: Channel): Boolean =
        channel.members.filter { user -> user.id == userId }.size == 1

    fun isOwner(userId: Int, channel: Channel): Boolean =
        channel.owner.id == userId
}
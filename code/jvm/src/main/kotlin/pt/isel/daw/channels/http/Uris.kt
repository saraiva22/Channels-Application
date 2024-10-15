package pt.isel.daw.channels.http

import org.springframework.web.util.UriTemplate
import java.net.URI

object Uris {
    const val PREFIX = "/api"
    const val HOME = PREFIX

    fun home(): URI = URI(HOME)

    object Users {
        const val CREATE = "$PREFIX/users"
        const val TOKEN = "$PREFIX/users/token"
        const val LOGOUT = "$PREFIX/logout"
        const val GET_BY_ID = "$PREFIX/users/{id}"
        const val INVITE = "$PREFIX/users/invite"
        const val HOME = "$PREFIX/me"

        fun byId(id: Int): URI = UriTemplate(GET_BY_ID).expand(id)

        fun home(): URI = URI(HOME)

        fun login(): URI = URI(TOKEN)

        fun register(): URI = URI(CREATE)
    }

    object Channels {
        const val CREATE = "$PREFIX/channels/create"
        const val JOIN_PUBLIC_CHANNELS = "$PREFIX/channels/public/{id}"
        const val JOIN_PRIVATE_CHANNELS = "$PREFIX/channels/private/{id}"
        const val UPDATE = "$PREFIX/channels/update/{id}"
        const val GET_BY_ID = "$PREFIX/channels/{id}"
        const val GET_BY_NAME = "$PREFIX/channels"
        const val GET_BY_USER = "$PREFIX/channels/user"
        const val GET_PUBLIC_CHANNELS = "$PREFIX/channels/public"
        const val CREATE_PRIVATE_INVITE = "$PREFIX/channels/private-invite/{id}"

        fun byId(id: Int): URI = UriTemplate(GET_BY_ID).expand(id)

        fun register(): URI = URI(CREATE)

        fun update(id: Int): URI = UriTemplate(UPDATE).expand(id)

        fun joinPublicChannel(id: Int): URI = UriTemplate(JOIN_PUBLIC_CHANNELS).expand(id)

        fun joinPrivateChannel(id: Int): URI = UriTemplate(JOIN_PRIVATE_CHANNELS).expand(id)

        fun invitePrivateChannel(id: Int) :URI = UriTemplate(CREATE_PRIVATE_INVITE).expand(id)
    }

    object Messages {
        const val CREATE = "$PREFIX/channels/{id}/create"
        const val GET_BY_CHANNEL = "$PREFIX/channels/{id}/messages"
        const val DELETE = "$PREFIX/channels/{channelId}/messages/{messageId}"

        fun register(): URI = URI(CREATE)

        fun byChannel(id: Int): URI = UriTemplate(GET_BY_CHANNEL).expand(id)
    }
}
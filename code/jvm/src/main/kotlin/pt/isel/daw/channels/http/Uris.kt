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
        const val HOME = "$PREFIX/me"

        fun byId(id: Int): URI = UriTemplate(GET_BY_ID).expand(id)

        fun home(): URI = URI(HOME)

        fun login(): URI = URI(TOKEN)

        fun register(): URI = URI(CREATE)
    }

    object Channels {
        const val CREATE = "$PREFIX/channels/create"
        const val JOIN = "$PREFIX/channels/join/{id}"
        const val UPDATE = "$PREFIX/channels/update/{id}"
        const val GET_BY_ID = "$PREFIX/channels/{id}"
        const val GET_BY_NAME = "$PREFIX/channels"
        const val GET_BY_USER = "$PREFIX/channels/user/{id}"
        const val GET_PUBLIC_CHANNELS = "$PREFIX/channels/public"


        fun byId(id: Int): URI = UriTemplate(GET_BY_ID).expand(id)
        fun register(): URI = URI(CREATE)
        fun update(id: Int): URI = UriTemplate(UPDATE).expand(id)

    }
}
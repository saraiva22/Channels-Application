package pt.isel.daw.channels.http

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
    }

    object Channels{
        const val CREATE = "$PREFIX/channels"
        const val JOIN = "$PREFIX/channels/{id}"
        const val UPDATE = "$PREFIX/channels/update/{id}"
    }
}
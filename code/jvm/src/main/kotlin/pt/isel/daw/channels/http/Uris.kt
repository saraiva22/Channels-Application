package pt.isel.daw.channels.http

import org.springframework.web.util.UriTemplate
import java.net.URI

object Uris {
    private const val PREFIX = "/api"
    private const val HOME = PREFIX

    fun home(): URI = URI(HOME)

    object Users {
        const val CREATE = "$PREFIX/users"
        const val TOKEN = "$PREFIX/users/token"
        const val LOGOUT = "$PREFIX/logout"
        const val GET_BY_ID = "$PREFIX/users/{id}"
        const val INVITE = "$PREFIX/users/invite"
        const val HOME = "$PREFIX/home"
        const val NOTIFICATIONS = "$PREFIX/users/notifications"

        fun byId(id: Int): URI = UriTemplate(GET_BY_ID).expand(id)
        fun home(): URI = URI(HOME)
        fun login(): URI = URI(TOKEN)
        fun logout(): URI = URI(LOGOUT)
        fun register(): URI = URI(CREATE)
    }

    object Channels {
        const val CREATE = "$PREFIX/channels/create"
        const val JOIN_PUBLIC_CHANNELS = "$PREFIX/channels/{id}"
        const val VALIDATE_CHANNEL_INVITE = "$PREFIX/channels/{id}/invite/{code}"
        const val UPDATE = "$PREFIX/channels/{id}/update"
        const val GET_BY_ID = "$PREFIX/channels/{id}"
        const val GET_BY_NAME = "$PREFIX/channels"
        const val GET_USER_OWNED_CHANNELS = "$PREFIX/channels/owner"
        const val GET_USER_MEMBER_CHANNELS = "$PREFIX/channels/member"
        const val GET_PUBLIC_CHANNELS = "$PREFIX/channels/public"
        const val CREATE_PRIVATE_INVITE = "$PREFIX/channels/{id}/private-invite"
        const val LEAVE_CHANNEL = "$PREFIX/channels/{id}/leave"
        const val RECEIVED_CHANNEL_INVITES = "$PREFIX/channels/invites/received"
        const val SENT_CHANNEL_INVITES = "$PREFIX/channels/invites/sent"
        const val BAN_USER = "$PREFIX/channels/{id}/ban"
        const val UNBAN_USER = "$PREFIX/channels/{id}/unban"

        fun byId(id: Int): URI = UriTemplate(GET_BY_ID).expand(id)
        fun register(): URI = URI(CREATE)
        fun update(id: Int): URI = UriTemplate(UPDATE).expand(id)
        fun joinPublicChannel(id: Int): URI = UriTemplate(JOIN_PUBLIC_CHANNELS).expand(id)
        fun validateChannelInvite(id: Int, code: String): URI = UriTemplate(VALIDATE_CHANNEL_INVITE).expand(id, code)
        fun invitePrivateChannel(id: Int): URI = UriTemplate(CREATE_PRIVATE_INVITE).expand(id)
        fun leaveChannel(id: Int): URI = UriTemplate(LEAVE_CHANNEL).expand(id)
        fun banUser(id: Int): URI = UriTemplate(BAN_USER).expand(id)
        fun unbanUser(id: Int): URI = UriTemplate(UNBAN_USER).expand(id)
    }


    object Messages {
        const val CREATE = "$PREFIX/channels/{id}/messages"
        const val GET_MESSAGES = "$PREFIX/channels/{id}/messages"
        const val DELETE = "$PREFIX/channels/{channelId}/messages/{id}"
        const val GET_BY_ID = "$PREFIX/channels/{channelId}/messages/{messageId}"


        fun register(): URI = URI(CREATE)
        fun byChannel(id: Int): URI = UriTemplate(GET_MESSAGES).expand(id)
        fun byId(channelId: Int, id: Int): URI = UriTemplate(GET_BY_ID).expand(channelId, id)
        fun create(id: Int): URI = UriTemplate(CREATE).expand(id)
        fun delete(channelId: Int, id: Int): URI = UriTemplate(DELETE).expand(channelId, id)
    }
}
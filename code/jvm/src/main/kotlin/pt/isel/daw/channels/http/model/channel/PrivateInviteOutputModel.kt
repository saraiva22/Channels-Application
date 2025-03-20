package pt.isel.daw.channels.http.model.channel

import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.channels.Status
import pt.isel.daw.channels.domain.user.UserInfo

data class PrivateInviteOutputModel(
    val codHash : String,
    val privacy: Privacy,
    val status: Status,
    val userInfo: UserInfo,
    val channelId: Int,
    val channelName: String
)

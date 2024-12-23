package pt.isel.daw.channels.http.model.channel

import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.user.User

data class RegisterPrivateInviteInputModel (
    val channelId: Int,
    val inviterId: User,
    val guestName: String,
    val inviteType: Privacy
)
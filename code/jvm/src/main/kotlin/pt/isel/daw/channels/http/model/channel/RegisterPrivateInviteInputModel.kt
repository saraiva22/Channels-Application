package pt.isel.daw.channels.http.model.channel

import pt.isel.daw.channels.domain.channels.Privacy

data class RegisterPrivateInviteInputModel (
    val channelId: Int,
    val inviterId: Int,
    val guestName: String,
    val inviteType: Privacy
)
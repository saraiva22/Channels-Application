package pt.isel.daw.channels.http.model.channel

import pt.isel.daw.channels.domain.channels.Status

data class JoinPrivateChannelInputModel(
    val status: Status
)

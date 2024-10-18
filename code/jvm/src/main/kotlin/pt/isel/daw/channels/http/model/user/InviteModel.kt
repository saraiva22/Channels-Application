package pt.isel.daw.channels.http.model.user

data class InviteModel (
    val codHash: String,
    val expired: Boolean
)
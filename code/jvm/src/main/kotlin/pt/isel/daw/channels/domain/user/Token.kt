package pt.isel.daw.channels.domain.user

import kotlinx.datetime.Instant
import pt.isel.daw.channels.domain.user.TokenValidationInfo

class Token(
    val tokenValidationInfo: TokenValidationInfo,
    val userId: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant,
)
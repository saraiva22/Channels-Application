package pt.isel.daw.channels.domain.user

import pt.isel.daw.channels.domain.user.TokenValidationInfo

interface TokenEncoder {
    fun createValidationInformation(token: String): TokenValidationInfo
}
package pt.isel.daw.channels.domain.token

interface TokenEncoder {
    fun createValidationInformation(token: String): TokenValidationInfo
}
package pt.isel.daw.channels.http.model.channel

import jakarta.validation.constraints.NotBlank
import pt.isel.daw.channels.domain.channels.Type

data class ChannelCreateInputModel (
    @field:NotBlank(message = "Name must not be blank")
    val name: String,
    val type: Type
) {
    init {
        require(name.isNotBlank())
    }
}
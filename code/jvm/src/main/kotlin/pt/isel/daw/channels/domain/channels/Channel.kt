package pt.isel.daw.channels.domain.channels

import pt.isel.daw.channels.domain.user.UserInfo

/**
 * Represent a Channel
 * @property id The unique
 * @property name The name of the channel
 * @property owner The owner of the channel
 * @property type The type of the channel
 * @property members The members of the channel
 */


data class Channel (
    val id: Int,
    val name: String,
    val owner: UserInfo,
    val type: Type,
    val members: List<UserInfo>,
    val bannedMembers: List<UserInfo>
)
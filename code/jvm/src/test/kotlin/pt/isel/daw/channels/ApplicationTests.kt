package pt.isel.daw.channels

import kotlin.math.abs
import kotlin.random.Random

open class ApplicationTests {
    companion object {
        fun newTestUserName() = "user-${abs(Random.nextLong())}"

        fun newTestEmail(username: String) = "$username@testmail.com"

        fun newTestChannelName() = "channel-${abs(Random.nextLong())}"

        fun newTokenValidationData() = "token-${abs(Random.nextLong())}"
    }
}
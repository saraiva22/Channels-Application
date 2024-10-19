package pt.isel.daw.channels

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.daw.channels.repository.configureWithAppRequirements
import kotlin.math.abs
import kotlin.random.Random

open class ApplicationTests {
    companion object {
        fun newTestUserName() = "user-${abs(Random.nextLong())}"

        fun newTestEmail(username: String) = "$username@testmail.com"

        fun newTestChannelName() = "channel-${abs(Random.nextLong())}"

        fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

        fun newMessageText() = "message-${abs(Random.nextLong())}"

        val jdbi =
            Jdbi.create(
                PGSimpleDataSource().apply {
                    setURL(Environment.getDbUrl())
                },
            ).configureWithAppRequirements()
    }
}
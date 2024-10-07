package pt.isel.daw.channels

import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.daw.channels.domain.user.Sha256TokenEncoder
import pt.isel.daw.channels.domain.user.UsersDomainConfig
import pt.isel.daw.channels.repository.Utils.configureWithAppRequirements
import kotlin.time.Duration.Companion.hours

@SpringBootApplication
class ChannelsApplication {

	@Bean
	fun jdbi(): Jdbi = Jdbi.create(
		PGSimpleDataSource().apply {
			//setURL(Environment.getDbUrl())
			setURL("jdbc:postgresql://localhost/DAW?user=postgres&password=12345")

		}
	).configureWithAppRequirements()

	@Bean
	fun passwordEncoder() = BCryptPasswordEncoder()

	@Bean
	fun tokenEncoder() = Sha256TokenEncoder()

	@Bean
	fun clock() = Clock.System

	@Bean
	fun usersDomainConfig() = UsersDomainConfig(
		tokenSizeInBytes = 256 / 8,
		tokenTtl = 24.hours,
		tokenRollingTtl = 1.hours,
		maxTokensPerUser = 3,
	)
}

fun main(args: Array<String>) {
	runApplication<ChannelsApplication>(*args)
}

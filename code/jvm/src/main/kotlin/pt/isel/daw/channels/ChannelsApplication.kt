package pt.isel.daw.channels

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import pt.isel.daw.channels.repository.Utils.configureWithAppRequirements

@SpringBootApplication
class ChannelsApplication {

	@Bean
	fun jdbi(): Jdbi = Jdbi.create(
		PGSimpleDataSource().apply {
			//setURL(Environment.getDbUrl())
			setURL("jdbc:postgresql://localhost/?user=postgres&password=daw")

		}
	).configureWithAppRequirements()
}

fun main(args: Array<String>) {
	runApplication<ChannelsApplication>(*args)
}

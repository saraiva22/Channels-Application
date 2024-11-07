package pt.isel.daw.channels.repository

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.domain.token.TokenValidationInfo
import pt.isel.daw.channels.domain.user.PasswordValidationInfo
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.repository.mappers.*

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    registerColumnMapper(PasswordValidationInfo::class.java, PasswordValidationInfoMapper())
    registerColumnMapper(TokenValidationInfo::class.java, TokenValidationInfoMapper())
    registerColumnMapper(Instant::class.java, InstantMapper())
    registerColumnMapper(Type::class.java, TypeMapper())

    registerRowMapper(User::class.java, UserMapper())
    registerRowMapper(Channel::class.java, ChannelMapper(UserInfoMapper(), TypeMapper()))

    return this
}
package pt.isel.daw.channels.repository.mappers

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.user.User
import java.sql.ResultSet


class ChannelMapper(private val ownerMapper: RowMapper<User>) : RowMapper<Channel> {
    override fun map(rs: ResultSet, ctx: StatementContext): Channel {
        //println(rs.getArray("members"))
        val owner = ownerMapper.map(rs, ctx)
        return Channel(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            owner = owner,
            members = emptyList()
        )
    }
}
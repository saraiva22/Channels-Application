package pt.isel.daw.channels.repository.mappers

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.daw.channels.domain.channels.Channel
import java.sql.ResultSet

class ChannelMapper : RowMapper<Channel> {
    override fun map(rs: ResultSet, ctx: StatementContext): Channel {
        println(rs.getArray("members"))

        return Channel(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            owner = rs.getInt("owner"),
            members = (rs.getArray("members").array as Array<Int>).toList()
        )
    }
}
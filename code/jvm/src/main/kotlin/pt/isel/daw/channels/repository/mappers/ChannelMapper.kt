package pt.isel.daw.channels.repository.mappers

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.daw.channels.domain.channels.Channel
import pt.isel.daw.channels.domain.channels.Type
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.domain.user.UserInfo
import java.sql.ResultSet

class ChannelMapper(
    private val ownerMapper: RowMapper<UserInfo>,
    private val typeMapper: ColumnMapper<Type>
) : RowMapper<Channel> {
    override fun map(rs: ResultSet, ctx: StatementContext): Channel {
        val owner = ownerMapper.map(rs, ctx)
        val type = typeMapper.map(rs, 2, ctx)
        return Channel(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            owner = owner,
            type = type,
            members = emptyList()
        )
    }
}
package pt.isel.daw.channels.repository.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.domain.user.UserInfo
import java.sql.ResultSet
import java.sql.SQLException

class UserInfoMapper : RowMapper<UserInfo> {
    @Throws(SQLException::class)
    override fun map(rs: ResultSet, ctx: StatementContext): UserInfo {
        return UserInfo(
            id = rs.getInt("id"),
            email = rs.getString("email"),
            username = rs.getString("username"),
        )
    }

}
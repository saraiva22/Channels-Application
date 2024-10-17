package pt.isel.daw.channels.repository.mappers

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.daw.channels.domain.user.PasswordValidationInfo
import pt.isel.daw.channels.domain.user.User
import java.sql.ResultSet
import java.sql.SQLException

class UserMapper : RowMapper<User> {
    @Throws(SQLException::class)
    override fun map(rs: ResultSet, ctx: StatementContext): User {
        return User(
            id = rs.getInt("id"),
            email = rs.getString("email"),
            username = rs.getString("username"),
            passwordValidation = PasswordValidationInfoMapper().map(rs, 4, ctx)
        )
    }

}
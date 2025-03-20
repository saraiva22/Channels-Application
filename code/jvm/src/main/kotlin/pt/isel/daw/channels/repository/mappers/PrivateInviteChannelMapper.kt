package pt.isel.daw.channels.repository.mappers

import org.jdbi.v3.core.statement.StatementContext
import pt.isel.daw.channels.domain.channels.Privacy
import pt.isel.daw.channels.domain.channels.Status
import pt.isel.daw.channels.domain.user.UserInfo
import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.daw.channels.http.model.channel.PrivateInviteOutputModel
import java.sql.ResultSet
import java.sql.SQLException

class PrivateInviteChannelMapper : RowMapper<PrivateInviteOutputModel> {
    @Throws(SQLException::class)
    override fun map(rs: ResultSet, ctx: StatementContext): PrivateInviteOutputModel {
        return PrivateInviteOutputModel(
            codHash = rs.getString("cod_hash"),
            privacy = Privacy.fromInt(rs.getInt("privacy")),
            status = Status.fromInt(rs.getInt("status")),
            userInfo = UserInfo(rs.getInt("id"), rs.getString("username"), rs.getString("email")),
            channelId = rs.getInt("private_ch"),
            channelName = rs.getString("name")
        )
    }

}
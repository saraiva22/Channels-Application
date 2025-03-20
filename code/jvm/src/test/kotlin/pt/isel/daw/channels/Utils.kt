package pt.isel.daw.channels

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import pt.isel.daw.channels.domain.user.UsersDomain
import java.security.MessageDigest

fun runWithHandle(jdbi: Jdbi, block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

fun clearData(jdbi: Jdbi, tableName: String, attributeName: String, value: Int) {
    runWithHandle(jdbi, { handle ->
        handle.createUpdate(
            """
                delete from $tableName
                where $attributeName = :value
            """
        )
            .bind("value", value)
            .execute()
    })
}

fun clearInvitationRegisterData(jdbi: Jdbi, value: String) {
    val md = MessageDigest.getInstance("SHA-256")
    val hashedBytes = md.digest(value.toByteArray())
    val hashedInvite =  hashedBytes.joinToString("") { "%02x".format(it) }

    runWithHandle(jdbi, { handle ->
        run {
            val inviteId = handle.createQuery(
                """
                    select id 
                    from dbo.Invitation_Register
                    where cod_hash = :value
                """
            )
                .bind("value", hashedInvite)
                .mapTo(Int::class.java)
                .one()

            clearData(jdbi, "dbo.Invitation_Register", "id", inviteId)
        }
    })
}
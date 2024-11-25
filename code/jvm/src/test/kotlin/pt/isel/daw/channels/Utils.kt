package pt.isel.daw.channels

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi

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
/*
fun clearInvitationChannelsData(jdbi: Jdbi, value: Int) {
    runWithHandle(jdbi, { handle ->
        run {
            val inviteIds = handle.createQuery(
                """
                select cod_hash
                from dbo.Invitation_Channels
                where inviter_id = :value
            """
            )
                .bind("value", value)
                .mapTo(String::class.java)
                .list()

            inviteIds.forEach { inviteId ->
                clearData(jdbi, "dbo.Invitation_Channels", "cod_hash", inviteId)
            }
        }
    })
}

 */

fun clearInvitationRegisterData(jdbi: Jdbi, value: String) {
    runWithHandle(jdbi, { handle ->
        run {
            val inviteId = handle.createQuery(
                """
                    select id 
                    from dbo.Invitation_Register
                    where cod_hash = :value
                """
            )
                .bind("value", value)
                .mapTo(Int::class.java)
                .one()

            clearData(jdbi, "dbo.Invitation_Register", "id", inviteId)
        }
    })
}
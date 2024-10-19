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

fun clearChannelsDataByType(jdbi: Jdbi, tableName: String, value: Int) {
    runWithHandle(jdbi, { handle ->
        handle.createUpdate(
            """
                delete from $tableName
                where channel_id in (
                    select id from dbo.Channels
                    where owner_id = :value
                )
            """
        )
            .bind("value", value)
            .execute()
    })
}

fun clearInvitationChannelsData(jdbi: Jdbi, value: Int) {
    runWithHandle(jdbi, { handle ->
        run {
            val inviteIds = handle.createQuery(
                """
                select invite_id 
                from dbo.Invite_Private_Channels
                where user_id = :value
            """
            )
                .bind("value", value)
                .mapTo(Int::class.java)
                .list()

            clearData(jdbi, "dbo.Invite_Private_Channels", "user_id", value)
            clearData(jdbi, "dbo.Invite_Private_Channels", "user_id", value)

            inviteIds.forEach { inviteId ->
                clearData(jdbi, "dbo.Invitation_Channels", "id", inviteId)
            }
        }
    })
}
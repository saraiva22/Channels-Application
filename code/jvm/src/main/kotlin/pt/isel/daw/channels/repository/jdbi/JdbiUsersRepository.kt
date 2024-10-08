package pt.isel.daw.channels.repository.jdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.slf4j.LoggerFactory
import pt.isel.daw.channels.domain.user.PasswordValidationInfo
import pt.isel.daw.channels.domain.token.Token
import pt.isel.daw.channels.domain.token.TokenValidationInfo
import pt.isel.daw.channels.domain.user.User
import pt.isel.daw.channels.repository.UsersRepository

class JdbiUsersRepository(
    private val handle: Handle
) : UsersRepository {
    override fun storeUser(username: String, email: String, passwordValidation: PasswordValidationInfo): Int {
        val userId = handle.createUpdate(
            """
            insert into dbo.Users (username, email, password_validation) values (:username, :email, :password_validation)
            """
        )
            .bind("username", username)
            .bind("email", email)
            .bind("password_validation", passwordValidation.validationInfo)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

        return userId
    }

    override fun getUserByUsername(username: String): User? =
        handle.createQuery("select * from dbo.Users where username = :username")
            .bind("username", username)
            .mapTo<User>()
            .singleOrNull()

    override fun getUserById(id: Int): User? =
        handle.createQuery("select * from dbo.Users where id = :id")
            .bind("id", id)
            .mapTo<User>()
            .singleOrNull()


    override fun isUserStoredByUsername(username: String): Boolean =
        handle.createQuery("select count(*) from dbo.Users where username = :username")
            .bind("username", username)
            .mapTo<Int>()
            .single() == 1

    override fun createToken(token: Token, maxTokens: Int) {
        val deletions = handle.createUpdate(
            """
            delete from dbo.Tokens 
            where user_id = :user_id 
                and token_validation in (
                    select token_validation from dbo.Tokens where user_id = :user_id 
                        order by last_used_at desc offset :offset
                )
            """.trimIndent(),
        )
            .bind("user_id", token.userId)
            .bind("offset", maxTokens - 1)
            .execute()

        logger.info("{} tokens deleted when creating new token", deletions)

        handle.createUpdate(
            """
                insert into dbo.Tokens(user_id, token_validation, created_at, last_used_at) 
                values (:user_id, :token_validation, :created_at, :last_used_at)
            """.trimIndent(),
        )
            .bind("user_id", token.userId)
            .bind("token_validation", token.tokenValidationInfo.validationInfo)
            .bind("created_at", token.createdAt.epochSeconds)
            .bind("last_used_at", token.lastUsedAt.epochSeconds)
            .execute()
    }

    override fun updateTokenLastUsed(token: Token, now: Instant) {
        handle.createUpdate(
            """
                update dbo.Tokens
                set last_used_at = :last_used_at
                where token_validation = :validation_information
            """.trimIndent(),
        )
            .bind("last_used_at", now.epochSeconds)
            .bind("validation_information", token.tokenValidationInfo.validationInfo)
            .execute()
    }

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>? =
        handle.createQuery(
            """
                select id, username, password_validation, token_validation, created_at, last_used_at
                from dbo.Users as users 
                inner join dbo.Tokens as tokens 
                on users.id = tokens.user_id
                where token_validation = :validation_information
            """,
        )
            .bind("validation_information", tokenValidationInfo.validationInfo)
            .mapTo<UserAndTokenModel>()
            .singleOrNull()
            ?.userAndToken


    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int {
        return handle.createUpdate(
            """
                delete from dbo.Tokens
                where token_validation = :validation_information
            """,
        )
            .bind("validation_information", tokenValidationInfo.validationInfo)
            .execute()
    }


    private data class UserAndTokenModel(
        val id: Int,
        val username: String,
        val email: String,
        val passwordValidation: PasswordValidationInfo,
        val tokenValidation: TokenValidationInfo,
        val createdAt: Long,
        val lastUsedAt: Long,
    ) {
        val userAndToken: Pair<User, Token>
            get() = Pair(
                User(id, username, email, passwordValidation),
                Token(
                    tokenValidation,
                    id,
                    Instant.fromEpochSeconds(createdAt),
                    Instant.fromEpochSeconds(lastUsedAt),
                ),
            )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JdbiUsersRepository::class.java)
    }

}
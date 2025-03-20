package pt.isel.daw.channels.http.pipeline

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import pt.isel.daw.channels.domain.user.AuthenticatedUser
import pt.isel.daw.channels.http.media.Problem.Companion.unauthorized

@Component
class AuthenticationInterceptor(
    private val authorizationHeaderProcessor: RequestTokenProcessor,
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod && handler.methodParameters.any {
                it.parameterType == AuthenticatedUser::class.java
            }
        ) {
            // enforce authentication
            val cookies = request.cookies
            val authHeader = authorizationHeaderProcessor
                .processAuthorizationHeaderValue(request.getHeader(NAME_AUTHORIZATION_HEADER))
            val user =
                if (cookies != null) {
                    val userToken = cookies.find { it.name == COOKIE_NAME_TOKEN }
                    if (userToken != null)
                        authorizationHeaderProcessor.processAuthorizationCookieValue(userToken)
                    else
                        authHeader
                } else
                    authHeader
            return if (user == null) {
                val objectMapper = ObjectMapper()
                val problem = unauthorized(unauthorized)
                val json = objectMapper.writeValueAsString(problem)
                response.writer.write(json)
                response.status = 401
                response.contentType = APPLICATION_PROBLEM_JSON_VALUE
                response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                false
            } else {
                AuthenticatedUserArgumentResolver.addUserTo(user, request)
                true
            }
        }

        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)
        const val NAME_AUTHORIZATION_HEADER = "Authorization"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
        const val COOKIE_NAME_TOKEN = "token"
    }
}
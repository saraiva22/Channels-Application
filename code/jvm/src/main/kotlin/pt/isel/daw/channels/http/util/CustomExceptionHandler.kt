package pt.isel.daw.channels.http.util

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import pt.isel.daw.channels.http.media.Problem

@ControllerAdvice
class CustomExceptionHandler : ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        log.info("Handling MethodArgumentNotValidException: {}", ex.message)
        val errorMessages = ex.bindingResult.fieldErrors.mapNotNull { it.defaultMessage }
        return Problem.response(400, Problem.invalidRequestContent(errorMessages))
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        log.info("Handling HttpMessageNotReadableException: {}", ex.message)
        return Problem.response(400, Problem.invalidRequestContent())
    }

    @ExceptionHandler(
        Exception::class,
    )
    fun handleAll(ex: Exception): ResponseEntity<Unit> {
        logger.info(ex.message)
        return ResponseEntity.status(500).build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(CustomExceptionHandler::class.java)
    }
}
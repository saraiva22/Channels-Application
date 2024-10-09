package pt.isel.daw.channels.http.util

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import pt.isel.daw.channels.http.Uris
import pt.isel.daw.channels.http.model.Problem
import pt.isel.daw.channels.http.model.Problem.Companion.invalidChannelType

@ControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidChannelTypeException(exception: HttpMessageNotReadableException): ResponseEntity<*> {
        return invalidChannelType(invalidChannelType)

    }

    companion object {
        private val log = LoggerFactory.getLogger(ExceptionHandler::class.java)
    }
}
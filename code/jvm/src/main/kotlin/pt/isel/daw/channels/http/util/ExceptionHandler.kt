package pt.isel.daw.channels.http.util

import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import pt.isel.daw.channels.http.model.Problem

@ControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidChannelTypeException(exception: HttpMessageNotReadableException): ResponseEntity<*> {
        return Problem.response(400, Problem.invalidChannelType)
    }
}
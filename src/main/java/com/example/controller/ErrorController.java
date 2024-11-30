package com.example.controller;

import com.example.dto.ApiError;
import com.example.exception.NotFoundException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.stream.Collectors;

import static com.example.dto.ErrorType.*;
import static io.micronaut.http.HttpStatus.*;

@Controller
public class ErrorController {

    @Error(exception = ConstraintViolationException.class, global = true)
    public HttpResponse<ApiError> handleValidationException(ConstraintViolationException ex) {
        return HttpResponse
                .status(BAD_REQUEST)
                .body(ApiError.builder()
                        .error(VALIDATION_ERROR)
                        .message(ex.getConstraintViolations()
                                .stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", ")))
                        .build());
    }

    @Error(exception = NotFoundException.class, global = true)
    public HttpResponse<ApiError> handleNotFoundException(NotFoundException ex) {
        return HttpResponse
                .status(NOT_FOUND)
                .body(ApiError.builder()
                        .error(NOT_FOUND_ERROR)
                        .message(ex.getMessage())
                        .build());
    }

    @Error(global = true)
    public HttpResponse<ApiError> handleGenericException(Throwable ex) {
        return HttpResponse
                .status(INTERNAL_SERVER_ERROR)
                .body(ApiError.builder()
                        .error(UNKNOWN_ERROR)
                        .message(ex.getMessage())
                        .build());
    }
}

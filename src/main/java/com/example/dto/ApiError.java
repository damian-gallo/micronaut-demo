package com.example.dto;

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.config.naming.SnakeCaseStrategy;
import lombok.Builder;

@Builder
@Serdeable(naming = SnakeCaseStrategy.class)
public record ApiError(
        ErrorType error,
        String message
) {
}

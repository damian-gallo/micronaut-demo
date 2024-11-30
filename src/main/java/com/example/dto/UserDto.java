package com.example.dto;

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.config.naming.SnakeCaseStrategy;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Serdeable(naming = SnakeCaseStrategy.class)
public record UserDto(
        UUID id,
        String name,
        String email,
        LocalDate birthdate,
        Gender gender,
        UserType type
) {
}

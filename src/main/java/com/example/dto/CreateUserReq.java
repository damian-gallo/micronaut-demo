package com.example.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.config.naming.SnakeCaseStrategy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Introspected
@Serdeable(naming = SnakeCaseStrategy.class)
@Builder
public record CreateUserReq(
        @NotNull(message = "Name is mandatory")
        String name,
        @NotNull(message = "Email is mandatory")
        @Email(message = "Invalid email format")
        String email,
        @NotNull(message = "Birthdate is mandatory")
        LocalDate birthdate,
        @NotNull(message = "Gender is mandatory")
        Gender gender,
        @NotNull(message = "Type is mandatory")
        UserType type
) {
}

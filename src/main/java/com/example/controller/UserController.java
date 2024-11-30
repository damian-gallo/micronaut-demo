package com.example.controller;

import com.example.dto.*;
import com.example.service.UserService;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.annotation.*;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Controller("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Get
    public CustomPage<UserDto> search(
            @QueryValue(value = "name") @Nullable String name,
            @QueryValue(value = "older_than") @Nullable Integer olderThan,
            @QueryValue(value = "types") @Nullable Set<UserType> types,
            @QueryValue(value = "gender") @Nullable Gender gender,
            Pageable pageable
    ) {
        var req = SearchUsersReq.builder()
                .name(name)
                .olderThan(olderThan)
                .types(types)
                .gender(gender)
                .build();

        return userService.search(req, pageable);
    }

    @Post
    public UserDto create(
            @Body @Valid CreateUserReq req
    ) {
        return userService.create(req);
    }

    @Get("/{id}")
    public UserDto getById(
            @PathVariable(name = "id") UUID id
    ) {
        return userService.getById(id);
    }
}

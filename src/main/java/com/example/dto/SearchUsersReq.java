package com.example.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public record SearchUsersReq(
        String name,
        Integer olderThan,
        Set<UserType> types,
        Gender gender
) {
}

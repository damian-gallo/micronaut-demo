package com.example.dto;

import io.micronaut.data.model.Page;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.config.naming.SnakeCaseStrategy;
import lombok.Builder;

import java.util.List;

@Builder
@Serdeable(naming = SnakeCaseStrategy.class)
public record CustomPage<T>(
        List<T> results,
        long pageSize,
        long pageNumber,
        long totalCount
) {

    public static <T> CustomPage<T> from(Page<T> page) {
        return CustomPage.<T>builder()
                .results(page.getContent())
                .pageSize(page.getPageable().getSize())
                .pageNumber(page.getPageable().getNumber())
                .totalCount(page.getTotalSize())
                .build();
    }
}
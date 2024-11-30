package com.example.persistence.repository;

import com.example.dto.Gender;
import com.example.dto.UserType;
import com.example.persistence.model.User;
import io.micronaut.data.repository.jpa.criteria.QuerySpecification;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.example.dto.Gender.FEMALE;
import static com.example.dto.Gender.MALE;
import static com.example.dto.UserType.*;
import static com.example.persistence.specification.UserSpecification.*;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@Sql(scripts = "feed-users.sql")
class UserRepositoryTest {

    @Inject
    UserRepository userRepository;

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2024-11-23T10:15:30Z"), ZoneId.of("UTC"));

    @ParameterizedTest
    @MethodSource("provideTestSoftDeleteArgs")
    void testSoftDelete(List<UUID> resultIds) {
        // When
        List<User> users = userRepository.findAll();

        // Then
        assertEquals(resultIds.size(), users.size());
        users.forEach(u -> assertTrue(resultIds.contains(u.getId())));
    }

    private static Stream<Arguments> provideTestSoftDeleteArgs() {
        return Stream.of(
                Arguments.of(List.of(
                        UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28"),
                        UUID.fromString("56a8d4b1-86a4-4dbf-9c29-12ed6d5010d3"),
                        UUID.fromString("83d03f79-d5e2-4990-9fbc-c3cd5c6c0172"),
                        UUID.fromString("9c5fbd1e-b6a7-4c3f-b5b3-24c3a9f1c982"),
                        UUID.fromString("a4e80f0e-6bfa-4e8f-9e42-48e37837c54f"),
                        UUID.fromString("cf458c3f-3eac-4f8e-abc6-75215eb8f774")))
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestNameLikeSpecArgs")
    void testNameLikeSpec(String name, List<UUID> resultIds) {
        // Given
        var spec = QuerySpecification.where(nameLike(name));

        // When
        List<User> users = userRepository.findAll(spec);

        // Then
        assertEquals(resultIds.size(), users.size());
        users.forEach(u -> assertTrue(resultIds.contains(u.getId())));
    }

    private static Stream<Arguments> provideTestNameLikeSpecArgs() {
        return Stream.of(
                Arguments.of("ohn", List.of(
                        UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28"),
                        UUID.fromString("1c1e3abc-14f2-4d6b-9b78-1b86d9fbb2a7"))),
                Arguments.of("li", List.of(
                        UUID.fromString("83d03f79-d5e2-4990-9fbc-c3cd5c6c0172"),
                        UUID.fromString("d9a63c7f-6a5f-4db7-9b7e-284e9348b5c9"))),
                Arguments.of("James", List.of(
                        UUID.fromString("cf458c3f-3eac-4f8e-abc6-75215eb8f774")))
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestOlderThanSpecArgs")
    void testOlderThanSpec(Integer yearsOld, List<UUID> resultIds) {
        // Given
        var spec = QuerySpecification.where(olderThan(yearsOld, CLOCK));

        // When
        List<User> users = userRepository.findAll(spec);

        // Then
        assertEquals(resultIds.size(), users.size());
        users.forEach(u -> assertTrue(resultIds.contains(u.getId())));
    }

    private static Stream<Arguments> provideTestOlderThanSpecArgs() {
        return Stream.of(
                Arguments.of(34, List.of(
                        UUID.fromString("56a8d4b1-86a4-4dbf-9c29-12ed6d5010d3"),
                        UUID.fromString("a4e80f0e-6bfa-4e8f-9e42-48e37837c54f"),
                        UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28"))),
                Arguments.of(50, emptyList())
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestTypeInSpecArgs")
    void testTypeInSpec(Set<UserType> types, List<UUID> resultIds) {
        // Given
        var spec = QuerySpecification.where(typeIn(types));

        // When
        List<User> users = userRepository.findAll(spec);

        // Then
        assertEquals(resultIds.size(), users.size());
        users.forEach(u -> assertTrue(resultIds.contains(u.getId())));
    }

    private static Stream<Arguments> provideTestTypeInSpecArgs() {
        return Stream.of(
                Arguments.of(Set.of(T1), List.of(
                        UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28"),
                        UUID.fromString("56a8d4b1-86a4-4dbf-9c29-12ed6d5010d3"),
                        UUID.fromString("a4e80f0e-6bfa-4e8f-9e42-48e37837c54f"),
                        UUID.fromString("cf458c3f-3eac-4f8e-abc6-75215eb8f774"))),
                Arguments.of(Set.of(T2, T3), List.of(
                        UUID.fromString("1c1e3abc-14f2-4d6b-9b78-1b86d9fbb2a7"),
                        UUID.fromString("743c9fdd-1e9c-40c7-87b6-3f21f6fae9ab"),
                        UUID.fromString("83d03f79-d5e2-4990-9fbc-c3cd5c6c0172"),
                        UUID.fromString("9c5fbd1e-b6a7-4c3f-b5b3-24c3a9f1c982"),
                        UUID.fromString("ba125b3b-b2ab-4f9b-99f1-93ceee52f781"),
                        UUID.fromString("d9a63c7f-6a5f-4db7-9b7e-284e9348b5c9")))
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestGenderEqualsSpecArgs")
    void testGenderEqualsSpec(Gender gender, List<UUID> resultIds) {
        // Given
        var spec = QuerySpecification.where(genderEquals(gender));

        // When
        List<User> users = userRepository.findAll(spec);

        // Then
        assertEquals(resultIds.size(), users.size());
        users.forEach(u -> assertTrue(resultIds.contains(u.getId())));
    }

    private static Stream<Arguments> provideTestGenderEqualsSpecArgs() {
        return Stream.of(
                Arguments.of(FEMALE, List.of(
                        UUID.fromString("1c1e3abc-14f2-4d6b-9b78-1b86d9fbb2a7"),
                        UUID.fromString("743c9fdd-1e9c-40c7-87b6-3f21f6fae9ab"),
                        UUID.fromString("ba125b3b-b2ab-4f9b-99f1-93ceee52f781"),
                        UUID.fromString("d9a63c7f-6a5f-4db7-9b7e-284e9348b5c9"),
                        UUID.fromString("9c5fbd1e-b6a7-4c3f-b5b3-24c3a9f1c982"))),
                Arguments.of(MALE, List.of(
                        UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28"),
                        UUID.fromString("56a8d4b1-86a4-4dbf-9c29-12ed6d5010d3"),
                        UUID.fromString("83d03f79-d5e2-4990-9fbc-c3cd5c6c0172"),
                        UUID.fromString("a4e80f0e-6bfa-4e8f-9e42-48e37837c54f"),
                        UUID.fromString("cf458c3f-3eac-4f8e-abc6-75215eb8f774")))
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestIsEnabledSpecArgs")
    void testIsEnabledSpec(boolean enabled, List<UUID> resultIds) {
        // Given
        var spec = QuerySpecification.where(isEnabled(enabled));

        // When
        List<User> users = userRepository.findAll(spec);

        // Then
        assertEquals(resultIds.size(), users.size());
        users.forEach(u -> assertTrue(resultIds.contains(u.getId())));
    }

    private static Stream<Arguments> provideTestIsEnabledSpecArgs() {
        return Stream.of(
                Arguments.of(true, List.of(
                        UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28"),
                        UUID.fromString("56a8d4b1-86a4-4dbf-9c29-12ed6d5010d3"),
                        UUID.fromString("83d03f79-d5e2-4990-9fbc-c3cd5c6c0172"),
                        UUID.fromString("9c5fbd1e-b6a7-4c3f-b5b3-24c3a9f1c982"),
                        UUID.fromString("a4e80f0e-6bfa-4e8f-9e42-48e37837c54f"),
                        UUID.fromString("cf458c3f-3eac-4f8e-abc6-75215eb8f774"))),
                Arguments.of(false, List.of(
                        UUID.fromString("1c1e3abc-14f2-4d6b-9b78-1b86d9fbb2a7"),
                        UUID.fromString("743c9fdd-1e9c-40c7-87b6-3f21f6fae9ab"),
                        UUID.fromString("ba125b3b-b2ab-4f9b-99f1-93ceee52f781"),
                        UUID.fromString("d9a63c7f-6a5f-4db7-9b7e-284e9348b5c9")))
        );
    }

}

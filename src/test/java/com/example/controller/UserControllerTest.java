package com.example.controller;

import com.example.dto.*;
import io.micronaut.core.type.GenericArgument;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.example.dto.ErrorType.*;
import static com.example.dto.Gender.MALE;
import static com.example.dto.UserType.T1;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@Sql(scripts = "feed-users.sql")
class UserControllerTest {

    @Inject
    private RequestSpecification spec;
    @Inject
    private ObjectMapper objectMapper;
    @Inject
    private Clock clock;

    @MockBean(Clock.class)
    public Clock clock() {
        return Clock.fixed(Instant.parse("2024-11-23T10:15:30Z"), ZoneId.of("UTC"));
    }

    private static final String NAME = "John Doe";
    private static final UserType TYPE = T1;
    private static final String EMAIL = "jdoe@gmail.com";
    private static final LocalDate BIRTHDATE = LocalDate.of(1994, 11, 15);
    private static final Gender GENDER = MALE;

    @Test
    void testCreateAndGetUser() throws IOException {
        String createUserResponseBody = spec
                .given()
                .contentType(JSON)
                .body(buildCreateUserRequestJson())
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .extract().body().asString();
        UserDto createdUser = objectMapper.readValue(createUserResponseBody, UserDto.class);

        String getUserByIdResponseBody = spec
                .given()
                .pathParam("id", createdUser.id())
                .when()
                .get("/users/{id}")
                .then()
                .statusCode(200)
                .extract().body().asString();
        UserDto retrievedUser = objectMapper.readValue(getUserByIdResponseBody, UserDto.class);

        assertEquals(createdUser, retrievedUser);
    }

    @Test
    void testGetUserWhenNotFound() throws IOException {
        String getUserByIdResponseBody = spec
                .given()
                .pathParam("id", "64e8a9f3-cf02-4a55-87bd-f1987cfac58d")
                .when()
                .get("/users/{id}")
                .then()
                .statusCode(404)
                .extract().body().asString();
        ApiError apiError = objectMapper.readValue(getUserByIdResponseBody, ApiError.class);

        assertEquals(NOT_FOUND_ERROR, apiError.error());
        assertEquals("User not found", apiError.message());
    }

    @Test
    void testGetUserWhenInvalidId() throws IOException {
        String getUserByIdResponseBody = spec
                .given()
                .pathParam("id", "some-invalid-id")
                .when()
                .get("/users/{id}")
                .then()
                .statusCode(500)
                .extract().body().asString();
        ApiError apiError = objectMapper.readValue(getUserByIdResponseBody, ApiError.class);

        assertEquals(UNKNOWN_ERROR, apiError.error());
        assertTrue(apiError.message().contains("Invalid UUID string"));
    }

    @ParameterizedTest
    @MethodSource("provideTestCreateUserWhenInvalidRequestArgs")
    void testCreateUserWhenInvalidRequest(
            String createUserRequestJson,
            String expectedMessage
    ) throws IOException {
        String createUserResponseBody = spec
                .given()
                .contentType(JSON)
                .body(createUserRequestJson)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .extract().body().asString();
        ApiError apiError = objectMapper.readValue(createUserResponseBody, ApiError.class);

        assertEquals(VALIDATION_ERROR, apiError.error());
        assertEquals(expectedMessage, apiError.message());
    }

    private static Stream<Arguments> provideTestCreateUserWhenInvalidRequestArgs() {
        return Stream.of(
                Arguments.of("""
                        {
                          "email": "jdoe@gmail.com",
                          "birthdate": "1994-11-15",
                          "gender": "MALE",
                          "type": "T1"
                        }""", "Name is mandatory"),
                Arguments.of("""
                        {
                          "name": "John Doe",
                          "birthdate": "1994-11-15",
                          "gender": "MALE",
                          "type": "T1"
                        }""", "Email is mandatory"),
                Arguments.of("""
                        {
                          "name": "John Doe",
                          "email": "jdoe",
                          "birthdate": "1994-11-15",
                          "gender": "MALE",
                          "type": "T1"
                        }""", "Invalid email format"),
                Arguments.of("""
                        {
                          "name": "John Doe",
                          "email": "jdoe@gmail.com",
                          "gender": "MALE",
                          "type": "T1"
                        }""", "Birthdate is mandatory"),
                Arguments.of("""
                        {
                          "name": "John Doe",
                          "email": "jdoe@gmail.com",
                          "birthdate": "1994-11-15",
                          "type": "T1"
                        }""", "Gender is mandatory"),
                Arguments.of("""
                        {
                          "name": "John Doe",
                          "email": "jdoe@gmail.com",
                          "birthdate": "1994-11-15",
                          "gender": "MALE"
                        }""", "Type is mandatory")

        );
    }

    @ParameterizedTest
    @MethodSource("provideTestSearchUsersParams")
    void testSearchUsers(
            Map<String, String> queryParams,
            int expectedPageNumber,
            int expectedPageSize,
            int expectedTotalCount,
            List<UUID> expectedUserIds
    ) throws IOException {
        String searchUsersResponseBody = spec
                .given()
                .queryParams(queryParams)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract().body().asString();
        CustomPage<UserDto> retrievedUsersPage = objectMapper.readValue(
                searchUsersResponseBody,
                new GenericArgument<>() {
                });

        assertEquals(expectedPageNumber, retrievedUsersPage.pageNumber());
        assertEquals(expectedPageSize, retrievedUsersPage.pageSize());
        assertEquals(expectedTotalCount, retrievedUsersPage.totalCount());
        for (UUID expectedUserId : expectedUserIds) {
            assertTrue(retrievedUsersPage.results()
                    .stream()
                    .anyMatch(u -> expectedUserId.equals(u.id())));
        }

    }

    private static Stream<Arguments> provideTestSearchUsersParams() {
        return Stream.of(
                // retrieve first page with default page, without filters
                Arguments.of(
                        Map.of(),
                        0,
                        100,
                        6,
                        List.of(
                                UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28"),
                                UUID.fromString("56a8d4b1-86a4-4dbf-9c29-12ed6d5010d3"),
                                UUID.fromString("83d03f79-d5e2-4990-9fbc-c3cd5c6c0172"),
                                UUID.fromString("9c5fbd1e-b6a7-4c3f-b5b3-24c3a9f1c982"),
                                UUID.fromString("a4e80f0e-6bfa-4e8f-9e42-48e37837c54f"),
                                UUID.fromString("cf458c3f-3eac-4f8e-abc6-75215eb8f774"))),
                // retrieve first page with custom page size, without filters
                Arguments.of(
                        Map.of(
                                "size", "1"
                        ),
                        0,
                        1,
                        6,
                        List.of(
                                UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28"))),
                // retrieve second page with custom page size, without filters
                Arguments.of(
                        Map.of(
                                "size", "1",
                                "page", "1"
                        ),
                        1,
                        1,
                        6,
                        List.of(
                                UUID.fromString("56a8d4b1-86a4-4dbf-9c29-12ed6d5010d3"))),
                // retrieve first page with default page, with filters (name)
                Arguments.of(
                        Map.of(
                                "name", "J"
                        ),
                        0,
                        100,
                        3,
                        List.of(
                                UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28"),
                                UUID.fromString("9c5fbd1e-b6a7-4c3f-b5b3-24c3a9f1c982"),
                                UUID.fromString("cf458c3f-3eac-4f8e-abc6-75215eb8f774"))),
                // retrieve first page with default page, with filters (name, gender)
                Arguments.of(
                        Map.of(
                                "name", "J",
                                "gender", "MALE"
                        ),
                        0,
                        100,
                        2,
                        List.of(
                                UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28"),
                                UUID.fromString("cf458c3f-3eac-4f8e-abc6-75215eb8f774"))),
                // retrieve first page with default page, with filters (name, gender, type)
                Arguments.of(
                        Map.of(
                                "name", "J",
                                "gender", "MALE",
                                "type", "T1"
                        ),
                        0,
                        100,
                        2,
                        List.of(
                                UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28"),
                                UUID.fromString("cf458c3f-3eac-4f8e-abc6-75215eb8f774"))),
                // retrieve first page with default page, with filters (name, gender, type, older_than)
                Arguments.of(
                        Map.of(
                                "name", "J",
                                "gender", "MALE",
                                "type", "T1",
                                "older_than", "34"
                        ),
                        0,
                        100,
                        1,
                        List.of(
                                UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28")))
        );
    }

    private static String buildCreateUserRequestJson() {
        return """
                {
                  "name": "%s",
                  "email": "%s",
                  "birthdate": "%s",
                  "gender": "%s",
                  "type": "%s"
                }
                """.formatted(NAME, EMAIL, BIRTHDATE, GENDER, TYPE);
    }

}

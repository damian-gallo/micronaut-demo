package com.example.service;

import com.example.dto.CreateUserReq;
import com.example.dto.Gender;
import com.example.dto.SearchUsersReq;
import com.example.dto.UserType;
import com.example.exception.NotFoundException;
import com.example.persistence.model.User;
import com.example.persistence.repository.UserRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.jpa.criteria.QuerySpecification;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.example.dto.Gender.MALE;
import static com.example.dto.UserType.T1;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
class UserServiceTest {

    @Inject
    private UserService userService;
    @Inject
    private UserRepository userRepository;

    @MockBean(UserRepository.class)
    public UserRepository userRepository() {
        return mock(UserRepository.class);
    }

    private static final String NAME = "John Doe";
    private static final UserType TYPE = T1;
    private static final String EMAIL = "jdoe@gmail.com";
    private static final LocalDate BIRTHDATE = LocalDate.of(1994, 11, 15);
    private static final Gender GENDER = MALE;
    private static final UUID ID = UUID.fromString("0f5df27d-a862-4fce-b791-c0b92cfd2e28");

    @Test
    void testCreateUser() {
        // Given
        var req = buildCreateUserRequest();
        when(userRepository.save(buildUser(null))).thenReturn(buildUser(ID));

        // When
        var user = userService.create(req);

        // Then
        assertEquals(ID, user.id());
        assertEquals(NAME, user.name());
        assertEquals(TYPE, user.type());
        assertEquals(EMAIL, user.email());
        assertEquals(BIRTHDATE, user.birthdate());
        assertEquals(GENDER, user.gender());
    }

    @Test
    void testSearch() {
        // Given
        var req = buildSearchUsersRequest();
        var pageable = Pageable.from(0, 10);
        when(userRepository.findAll(ArgumentMatchers.<QuerySpecification<User>>any(), eq(pageable)))
                .thenReturn(Page.of(List.of(buildUser(ID)), pageable, 1L));

        // When
        var searchResult = userService.search(req, pageable);

        // Then
        assertEquals(10, searchResult.pageSize());
        assertEquals(0, searchResult.pageNumber());
        assertEquals(1, searchResult.totalCount());
        assertEquals(ID, searchResult.results().getFirst().id());
        assertEquals(NAME, searchResult.results().getFirst().name());
        assertEquals(TYPE, searchResult.results().getFirst().type());
        assertEquals(EMAIL, searchResult.results().getFirst().email());
        assertEquals(BIRTHDATE, searchResult.results().getFirst().birthdate());
        assertEquals(GENDER, searchResult.results().getFirst().gender());
    }

    @Test
    void testGetById() {
        // Given
        when(userRepository.findById(ID)).thenReturn(Optional.of(buildUser(ID)));

        // When
        var user = userService.getById(ID);

        // Then
        assertEquals(ID, user.id());
        assertEquals(NAME, user.name());
        assertEquals(TYPE, user.type());
        assertEquals(EMAIL, user.email());
        assertEquals(BIRTHDATE, user.birthdate());
        assertEquals(GENDER, user.gender());
    }

    @Test
    void testGetByIdWhenNotFound() {
        // Given
        when(userRepository.findById(ID)).thenReturn(empty());

        // When
        var ex = assertThrows(NotFoundException.class, () -> userService.getById(ID));

        // Then
        assertEquals("User not found", ex.getMessage());
    }

    private static CreateUserReq buildCreateUserRequest() {
        return CreateUserReq.builder()
                .name(NAME)
                .type(TYPE)
                .email(EMAIL)
                .birthdate(BIRTHDATE)
                .gender(GENDER)
                .build();
    }

    private static User buildUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setName(NAME);
        user.setType(TYPE);
        user.setEmail(EMAIL);
        user.setBirthdate(BIRTHDATE);
        user.setGender(GENDER);

        return user;
    }

    private static SearchUsersReq buildSearchUsersRequest() {
        return SearchUsersReq.builder()
                .name(NAME)
                .olderThan(25)
                .gender(GENDER)
                .types(Set.of(T1))
                .build();
    }

}

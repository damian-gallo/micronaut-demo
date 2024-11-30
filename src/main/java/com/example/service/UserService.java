package com.example.service;

import com.example.dto.CreateUserReq;
import com.example.dto.CustomPage;
import com.example.dto.SearchUsersReq;
import com.example.dto.UserDto;
import com.example.exception.NotFoundException;
import com.example.persistence.model.User;
import com.example.persistence.repository.UserRepository;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.jpa.criteria.QuerySpecification;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.UUID;

import static com.example.persistence.specification.UserSpecification.*;

@Singleton
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final Clock clock;

    public UserDto create(CreateUserReq req) {
        User user = new User();
        user.setName(req.name());
        user.setEmail(req.email());
        user.setBirthdate(req.birthdate());
        user.setGender(req.gender());
        user.setType(req.type());

        user = userRepository.save(user);

        return toDto(user);
    }

    public CustomPage<UserDto> search(SearchUsersReq req, Pageable pageable) {
        var spec = QuerySpecification.where(nameLike(req.name()))
                .and(olderThan(req.olderThan(), clock))
                .and(typeIn(req.types()))
                .and(genderEquals(req.gender()))
                .and(isEnabled(true));

        var page = userRepository.findAll(spec, pageable).map(this::toDto);

        return CustomPage.from(page);
    }

    public UserDto getById(UUID id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .birthdate(user.getBirthdate())
                .gender(user.getGender())
                .type(user.getType())
                .build();
    }
}

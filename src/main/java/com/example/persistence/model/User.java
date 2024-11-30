package com.example.persistence.model;

import com.example.dto.Gender;
import com.example.dto.UserType;
import io.micronaut.data.annotation.Where;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.UUID;

@Serdeable
@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "users")
@Where("@.enabled = true")
public class User {

    @Id
    @GeneratedValue(strategy = UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "gender")
    @Enumerated(STRING)
    private Gender gender;

    @Column(name = "type")
    @Enumerated(STRING)
    private UserType type;

    @Column(name = "enabled")
    private boolean enabled = true;
}

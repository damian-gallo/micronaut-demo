package com.example.persistence.specification;

import com.example.dto.Gender;
import com.example.dto.UserType;
import com.example.persistence.model.User;
import io.micronaut.data.repository.jpa.criteria.QuerySpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;

public class UserSpecification {

    private UserSpecification() {
    }

    public static QuerySpecification<User> nameLike(String name) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                name != null
                        ? cb.like(root.get("name"), "%" + name + "%")
                        : null;
    }

    public static QuerySpecification<User> olderThan(Integer yearsOld, Clock clock) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                yearsOld != null
                        ? cb.lessThan(root.get("birthdate"), LocalDate.now(clock).minus(Period.ofYears(yearsOld)))
                        : null;
    }

    public static QuerySpecification<User> typeIn(Set<UserType> types) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                types != null && !types.isEmpty()
                        ? root.get("type").in(types)
                        : null;
    }

    public static QuerySpecification<User> genderEquals(Gender gender) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                gender != null
                        ? cb.equal(root.get("gender"), gender)
                        : null;
    }

    public static QuerySpecification<User> isEnabled(boolean enabled) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                cb.equal(root.get("enabled"), enabled);
    }
}
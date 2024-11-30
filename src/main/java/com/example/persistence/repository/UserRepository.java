package com.example.persistence.repository;

import com.example.persistence.model.User;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.repository.jpa.JpaSpecificationExecutor;

import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID>, JpaSpecificationExecutor<User> {

}

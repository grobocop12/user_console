package com.grobocop.userconsole.data;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findUserEntityByUsername(final String username);
}

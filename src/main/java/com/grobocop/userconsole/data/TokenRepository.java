package com.grobocop.userconsole.data;

import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Date;

public interface TokenRepository extends CrudRepository<TokenEntity, String> {
    Collection<TokenEntity> findAllByUsername(String username);

    Collection<TokenEntity> findAllByUsernameAndEnabled(String username,
                                                        boolean enabled);

    Collection<TokenEntity> findAllByRefreshTokenExpirationBefore(Date date);
}

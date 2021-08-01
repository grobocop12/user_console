package com.grobocop.userconsole.data;

import org.springframework.data.repository.CrudRepository;

public interface TokenRepository extends CrudRepository<TokenEntity, String> {
}

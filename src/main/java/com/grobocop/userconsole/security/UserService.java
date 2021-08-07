package com.grobocop.userconsole.security;

import com.grobocop.userconsole.data.UserEntity;

public interface UserService {
    UserEntity addUser(final String name, final String rawPassword);

    UserEntity addAdmin(String username, String rawPassword);
}

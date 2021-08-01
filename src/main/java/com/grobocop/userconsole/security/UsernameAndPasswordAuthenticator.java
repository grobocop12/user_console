package com.grobocop.userconsole.security;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@AllArgsConstructor
public class UsernameAndPasswordAuthenticator {
    private final AuthenticationManager authenticationManager;

    public Authentication authenticate(final String username, final String password) {
        final Authentication auth = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(auth);
    }
}

package com.grobocop.userconsole.security;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@AllArgsConstructor
public class UsernameAndPasswordAuthenticator {
    private final AuthenticationManager authenticationManager;

    public Authentication authenticate(final String username, final String password) {
        final Authentication auth = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(auth);
    }

    public Authentication authenticate(final String username, final Collection<GrantedAuthority> authorities) {
        final Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
        return authenticationManager.authenticate(auth);
    }

}

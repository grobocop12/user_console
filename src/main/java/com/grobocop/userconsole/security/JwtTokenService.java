package com.grobocop.userconsole.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;

public class JwtTokenService {
    private final AuthenticationManager authenticationManager;
    private final String secretKey;

    public JwtTokenService(final AuthenticationManager authenticationManager,
                           final String secretKey) {
        this.authenticationManager = authenticationManager;
        this.secretKey = secretKey;
    }

    public String createToken(String username, String password) {
        final UsernamePasswordAuthenticationToken usernamePasswordAuth =
                new UsernamePasswordAuthenticationToken(username, password);
        final Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuth);
        if (!authenticate.isAuthenticated()) {
            throw new RuntimeException("");
        }
        Collection<? extends GrantedAuthority> authorities = authenticate.getAuthorities();
        final Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        Jwts.builder()
                .setSubject(username)
                .signWith(key)
                .compact();

        return "";
    }
}

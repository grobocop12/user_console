package com.grobocop.userconsole.security;

import com.grobocop.userconsole.data.TokenEntity;
import com.grobocop.userconsole.data.TokenRepository;
import com.grobocop.userconsole.util.DateAndTimeProvider;
import com.grobocop.userconsole.web.request.AuthenticationRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {
    private static final long FIFTEEN_MINUTES_IN_MILLISECONDS = 15 * 60 * 1000;
    private static final String AGENT_HEADER = "user-agent";

    private final UsernameAndPasswordAuthenticator authenticator;
    private final String secretKey;
    private final DateAndTimeProvider dateAndTimeProvider;
    private final TokenRepository tokenRepository;

    @Autowired
    public JwtTokenService(final UsernameAndPasswordAuthenticator authenticator,
                           @Value("${secretKey}") final String secretKey,
                           final DateAndTimeProvider dateAndTimeProvider,
                           final TokenRepository tokenRepository) {
        this.authenticator = authenticator;
        this.secretKey = secretKey;
        this.dateAndTimeProvider = dateAndTimeProvider;
        this.tokenRepository = tokenRepository;
    }

    public TokenEntity prepareTokenResponse(final AuthenticationRequest authRequest, final HttpServletRequest servletRequest) {
        final Authentication authResult = authenticator.authenticate(authRequest.getUsername(),
                authRequest.getPassword());
        if (!authResult.isAuthenticated()) {
            throw new RuntimeException("Wrong username or password!");
        }
        final TokenEntity tokenPrototype = prepareTokenEntity(authResult.getName(), servletRequest);
        final String token = buildAccessToken(tokenPrototype, authResult.getAuthorities());
        tokenPrototype.setAccessToken(token);
        saveTokenEntity(tokenPrototype);
        return tokenPrototype;
    }

    private TokenEntity prepareTokenEntity(final String username, final HttpServletRequest servletRequest) {
        final Date issuedDate = dateAndTimeProvider.getCurrentDate();
        final Date expirationDate = new Date(issuedDate.getTime() + FIFTEEN_MINUTES_IN_MILLISECONDS);
        final String id = UUID.randomUUID().toString();
        final String remoteAddr = servletRequest.getRemoteAddr();
        final String header = servletRequest.getHeader(AGENT_HEADER);
        return TokenEntity.builder()
                .id(id)
                .username(username)
                .ip(remoteAddr)
                .agent(header)
                .issuedAt(issuedDate)
                .expires(expirationDate)
                .build();
    }

    private void saveTokenEntity(final TokenEntity entity) {
        tokenRepository.save(entity);
    }

    private String buildAccessToken(final TokenEntity tokenPrototype, final Collection<? extends GrantedAuthority> authorities) {
        final Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setHeaderParam("id", tokenPrototype.getId())
                .setHeaderParam("ip", tokenPrototype.getIp())
                .setHeaderParam("agent", tokenPrototype.getAgent())
                .setSubject(tokenPrototype.getUsername())
                .claim("authorities", authorities)
                .setIssuedAt(tokenPrototype.getIssuedAt())
                .setExpiration(tokenPrototype.getExpires())
                .signWith(key)
                .compact();
    }

}

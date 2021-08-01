package com.grobocop.userconsole.security;

import com.grobocop.userconsole.data.TokenEntity;
import com.grobocop.userconsole.data.TokenRepository;
import com.grobocop.userconsole.exception.AuthenticationException;
import com.grobocop.userconsole.util.DateAndTimeProvider;
import com.grobocop.userconsole.web.request.AuthenticationRequest;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {
    private static final long FIFTEEN_MINUTES_IN_MILLISECONDS = 15 * 60 * 1000;
    private static final String AGENT_HEADER = "user-agent";
    private static final String JWT_AGENT_HEADER = "agent";
    private final static String JWT_IP_HEADER = "ip";
    private final static String JWT_ID_HEADER = "id";
    private final static String JWT_AUTHORITIES_CLAIM = "authorities";

    private final UsernameAndPasswordAuthenticator authenticator;
    private final KeyProvider keyProvider;
    private final DateAndTimeProvider dateAndTimeProvider;
    private final TokenRepository tokenRepository;

    @Autowired
    public JwtTokenService(final UsernameAndPasswordAuthenticator authenticator,
                           final KeyProvider keyProvider,
                           final DateAndTimeProvider dateAndTimeProvider,
                           final TokenRepository tokenRepository) {
        this.authenticator = authenticator;
        this.keyProvider = keyProvider;
        this.dateAndTimeProvider = dateAndTimeProvider;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public TokenEntity prepareTokenResponse(final AuthenticationRequest authRequest, final HttpServletRequest servletRequest) {
        final Authentication authResult = authenticateUser(authRequest.getUsername(), authRequest.getPassword());
        final TokenEntity tokenPrototype = prepareTokenEntity(authResult.getName(), servletRequest);
        final String token = buildAccessToken(tokenPrototype, authResult.getAuthorities());
        tokenPrototype.setAccessToken(token);
        blacklistTokensForUsername(authResult.getName());
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
                .enabled(true)
                .build();
    }

    private Authentication authenticateUser(final String username, final String password) {
        try {
            return authenticator.authenticate(username,
                    password);
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Wrong username or password", e);
        }
    }

    private void blacklistTokensForUsername(String username) {
        Iterable<TokenEntity> tokens = tokenRepository.findAllByUsername(username);
        tokens.forEach(t -> t.setEnabled(false));
        tokenRepository.saveAll(tokens);
    }

    private void saveTokenEntity(final TokenEntity entity) {
        tokenRepository.save(entity);
    }

    private String buildAccessToken(final TokenEntity tokenPrototype, final Collection<? extends GrantedAuthority> authorities) {
        return Jwts.builder()
                .setHeaderParam(JWT_ID_HEADER, tokenPrototype.getId())
                .setHeaderParam(JWT_IP_HEADER, tokenPrototype.getIp())
                .setHeaderParam(JWT_AGENT_HEADER, tokenPrototype.getAgent())
                .setSubject(tokenPrototype.getUsername())
                .claim(JWT_AUTHORITIES_CLAIM, authorities)
                .setIssuedAt(tokenPrototype.getIssuedAt())
                .setExpiration(tokenPrototype.getExpires())
                .signWith(keyProvider.getKey())
                .compact();
    }


}

package com.grobocop.userconsole.security.jwt;

import com.grobocop.userconsole.data.TokenEntity;
import com.grobocop.userconsole.exception.AuthenticationException;
import com.grobocop.userconsole.security.KeyProvider;
import com.grobocop.userconsole.security.UsernameAndPasswordAuthenticator;
import com.grobocop.userconsole.util.DateAndTimeProvider;
import com.grobocop.userconsole.web.response.TokenResponse;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;

import static java.util.Collections.singletonList;

@Service
@RequiredArgsConstructor
public class JwtCreatorImpl implements JwtCreator {
    private static final long FIFTEEN_MINUTES_IN_MILLISECONDS = 15 * 60 * 1000;
    private static final long ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000;
    private final static String JWT_AUTHORITIES_CLAIM = "authorities";

    private final UsernameAndPasswordAuthenticator authenticator;
    private final KeyProvider keyProvider;
    private final DateAndTimeProvider dateAndTimeProvider;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;

    @Override
    public TokenResponse createAccessTokenAndRefreshToken(final String username, final String password) {
        final Authentication authentication = authenticateUser(username, password);
        return createAndSaveTokens(authentication.getName(), authentication.getAuthorities());
    }

    @Override
    public TokenResponse refreshToken(final String username) {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return createAndSaveTokens(userDetails.getUsername(), userDetails.getAuthorities());
    }

    private Authentication authenticateUser(final String username, final String password) {
        try {
            return authenticator.authenticate(username, password);
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Wrong username or password", e);
        }
    }

    private TokenResponse createAndSaveTokens(String username, Collection<? extends GrantedAuthority> authorities) {
        final TokenInternalRequest internalRequest = prepareInternalRequest(username, authorities);
        final TokenEntity tokenEntity = processInternalRequest(internalRequest);
        tokenService.saveNewTokenAndBlacklistOld(tokenEntity);
        return prepareResponse(tokenEntity);
    }

    private TokenInternalRequest prepareInternalRequest(String username, Collection<? extends GrantedAuthority> authorities) {
        final Date currentDate = dateAndTimeProvider.getCurrentDate();
        return TokenInternalRequest.builder()
                .username(username)
                .authorities(authorities)
                .issuedAt(currentDate)
                .accessTokenExpiration(new Date(currentDate.getTime() + FIFTEEN_MINUTES_IN_MILLISECONDS))
                .refreshTokenExpiration(new Date(currentDate.getTime() + ONE_HOUR_IN_MILLISECONDS))
                .build();
    }

    private TokenEntity processInternalRequest(TokenInternalRequest internalRequest) {
        final String accessToken = Jwts.builder()
                .setSubject(internalRequest.getUsername())
                .claim(JWT_AUTHORITIES_CLAIM, internalRequest.getAuthorities())
                .setIssuedAt(internalRequest.getIssuedAt())
                .setExpiration(internalRequest.getAccessTokenExpiration())
                .signWith(keyProvider.getKey())
                .compact();
        final String refreshToken = Jwts.builder()
                .setSubject(internalRequest.getUsername())
                .claim(JWT_AUTHORITIES_CLAIM, singletonList(new SimpleGrantedAuthority("ROLE_REFRESH")))
                .setIssuedAt(internalRequest.getIssuedAt())
                .setExpiration(internalRequest.getRefreshTokenExpiration())
                .signWith(keyProvider.getKey())
                .compact();
        return TokenEntity.builder()
                .username(internalRequest.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .issuedAt(internalRequest.getIssuedAt())
                .accessTokenExpiration(internalRequest.getAccessTokenExpiration())
                .refreshTokenExpiration(internalRequest.getRefreshTokenExpiration())
                .enabled(true)
                .build();

    }

    private TokenResponse prepareResponse(TokenEntity tokenEntity) {
        return TokenResponse.builder()
                .accessToken(tokenEntity.getAccessToken())
                .refreshToken(tokenEntity.getRefreshToken())
                .accessTokenExpiration(tokenEntity.getAccessTokenExpiration().getTime())
                .refreshTokenExpiration(tokenEntity.getRefreshTokenExpiration().getTime())
                .build();
    }

}

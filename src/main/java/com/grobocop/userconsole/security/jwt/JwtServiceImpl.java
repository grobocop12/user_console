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
public class JwtServiceImpl implements JwtService {
    private static final long FIFTEEN_MINUTES_IN_MILLISECONDS = 1 * 60 * 1000;
    private static final long ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000;
    private final static String JWT_AUTHORITIES_CLAIM = "authorities";

    private final UsernameAndPasswordAuthenticator authenticator;
    private final KeyProvider keyProvider;
    private final DateAndTimeProvider dateAndTimeProvider;
    private final UserDetailsService userDetailsService;

    @Override
    public TokenResponse createAccessTokenAndRefreshToken(final String username, final String password) {
        final Authentication authentication = authenticateUser(username, password);
        final TokenInternalRequest internalRequest = prepareInternalRequest(authentication.getName(), authentication.getAuthorities());
        return processInternalRequest(internalRequest);
    }

    @Override
    public TokenResponse refreshToken(final String username) {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final TokenInternalRequest internalRequest = prepareInternalRequest(userDetails.getUsername(), userDetails.getAuthorities());
        return processInternalRequest(internalRequest);
    }

    private Authentication authenticateUser(final String username, final String password) {
        try {
            return authenticator.authenticate(username, password);
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Wrong username or password", e);
        }
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

    private TokenResponse processInternalRequest(TokenInternalRequest internalRequest) {
        final TokenEntity tokenEntity = createTokenEntity(internalRequest);
        return TokenResponse.builder()
                .accessToken(tokenEntity.getAccessToken())
                .refreshToken(tokenEntity.getRefreshToken())
                .accessTokenExpiration(internalRequest.getAccessTokenExpiration().getTime())
                .refreshTokenExpiration(internalRequest.getRefreshTokenExpiration().getTime())
                .build();
    }

    private TokenEntity createTokenEntity(TokenInternalRequest internalRequest) {
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
                .accessTokenExpiration(internalRequest.getAccessTokenExpiration())
                .refreshTokenExpiration(internalRequest.getRefreshTokenExpiration())
                .build();
    }

}

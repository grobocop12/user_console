package com.grobocop.userconsole.security.jwt;

import com.grobocop.userconsole.data.TokenEntity;
import com.grobocop.userconsole.security.KeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class JwtTokenVerifier extends OncePerRequestFilter {
    private final static String AUTHORIZATION_HEADER = "Authorization";

    private final KeyProvider keyProvider;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            final String token = authorizationHeader.replace("Bearer ", "");
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(keyProvider.getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            if (isBlacklisted(claims.getSubject(), token)) {
                throw new IllegalStateException("Invalid token.");
            }
            setAuthentication(claims);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalStateException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token.");
        }

    }

    private void setAuthentication(final Claims claims) {
        final String username = claims.getSubject();
        final List<Map<String, String>> authorities = (List<Map<String, String>>) claims.get("authorities");
        final Set<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                .map(m -> new SimpleGrantedAuthority(m.get("authority")))
                .collect(Collectors.toSet());
        final Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                grantedAuthorities
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isBlacklisted(String username, String token) {
        final Collection<TokenEntity> tokens = tokenService.findDisabledTokensOfUser(username);
        return tokens.stream().anyMatch(t -> t.getAccessToken().equals(token)
                || t.getRefreshToken().equals(token));
    }
}

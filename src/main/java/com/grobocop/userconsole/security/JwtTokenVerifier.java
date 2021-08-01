package com.grobocop.userconsole.security;

import io.jsonwebtoken.*;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.security.sasl.AuthenticationException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class JwtTokenVerifier extends OncePerRequestFilter {
    private final static String AUTHORIZATION_HEADER = "Authorization";
    private final static String USER_AGENT_HEADER = "user-agent";
    private final static String JWT_AGENT_HEADER = "agent";
    private final static String JWT_IP_HEADER = "ip";

    private final KeyProvider keyProvider;
    private final BlacklistedTokenService blacklistedTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            final String token = authorizationHeader.replace("Bearer ", "");
            final Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(keyProvider.getKey())
                    .build()
                    .parseClaimsJws(token);
            if(blacklistedTokenService.isBlackListed(claimsJws.getBody().getSubject(), token)) {
                throw new IllegalStateException("Invalid token.");
            }
            validateHeader(claimsJws.getHeader(), request);
            setAuthentication(claimsJws.getBody());
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalStateException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token.");
        }

    }

    private void validateHeader(JwsHeader header, HttpServletRequest request) {
        final String ip = (String) header.get(JWT_IP_HEADER);
        final String agent = (String) header.get(JWT_AGENT_HEADER);
        final String servletAgent = request.getHeader(USER_AGENT_HEADER);
        if (!request.getRemoteAddr().equals(ip) || !servletAgent.equals(agent)) {
            throw new IllegalStateException("Token cannot be trusted");
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
}

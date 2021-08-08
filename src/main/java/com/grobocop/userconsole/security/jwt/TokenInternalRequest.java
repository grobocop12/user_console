package com.grobocop.userconsole.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TokenInternalRequest {
    private String username;
    private Collection<? extends GrantedAuthority> authorities;
    private Date issuedAt;
    private Date accessTokenExpiration;
    private Date refreshTokenExpiration;
}

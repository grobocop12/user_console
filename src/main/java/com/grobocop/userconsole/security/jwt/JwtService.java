package com.grobocop.userconsole.security.jwt;

import com.grobocop.userconsole.web.response.TokenResponse;

public interface JwtService {
    TokenResponse createAccessTokenAndRefreshToken(String username, String password);
    TokenResponse refreshToken(String username);
}

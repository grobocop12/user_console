package com.grobocop.userconsole.security.jwt;

import com.grobocop.userconsole.web.response.TokenResponse;

public interface JwtCreator {
    TokenResponse createAccessTokenAndRefreshToken(String username, String password);

    TokenResponse refreshToken(String username);
}

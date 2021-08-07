package com.grobocop.userconsole.security.jwt;

import com.grobocop.userconsole.web.response.TokenResponse;

public interface JwtTokenService {
    TokenResponse createAccessTokenAndRefreshToken(String username, String password);
}

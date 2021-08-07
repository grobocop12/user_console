package com.grobocop.userconsole.web;

import com.grobocop.userconsole.security.jwt.JwtTokenService;
import com.grobocop.userconsole.web.request.AuthenticationRequest;
import com.grobocop.userconsole.web.response.TokenResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthenticationApi {
    private final JwtTokenService jwtTokenService;

    @PostMapping("/login")
    public TokenResponse createToken(@RequestBody AuthenticationRequest authRequest) {
        return jwtTokenService.createAccessTokenAndRefreshToken(authRequest.getUsername(), authRequest.getPassword());
    }
}

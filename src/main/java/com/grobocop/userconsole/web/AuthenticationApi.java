package com.grobocop.userconsole.web;

import com.grobocop.userconsole.security.jwt.JwtService;
import com.grobocop.userconsole.web.request.AuthenticationRequest;
import com.grobocop.userconsole.web.response.TokenResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthenticationApi {
    private final JwtService jwtService;

    @PostMapping("/login")
    public TokenResponse createToken(@RequestBody AuthenticationRequest authRequest) {
        return jwtService.createAccessTokenAndRefreshToken(authRequest.getUsername(), authRequest.getPassword());
    }

    @PostMapping("/refresh")
    public TokenResponse refreshToken(HttpServletRequest request) {
        final Principal userPrincipal = request.getUserPrincipal();
        return jwtService.refreshToken(userPrincipal.getName());
    }
}

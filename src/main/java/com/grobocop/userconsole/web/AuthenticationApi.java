package com.grobocop.userconsole.web;

import com.grobocop.userconsole.security.JwtTokenService;
import com.grobocop.userconsole.web.request.AuthenticationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationApi {
    private final JwtTokenService jwtTokenService;

    @Autowired
    public AuthenticationApi(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public String createToken(@RequestBody AuthenticationRequest authRequest) {
        return jwtTokenService.createToken(authRequest.getUsername(), authRequest.getPassword());
    }
}

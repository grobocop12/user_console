package com.grobocop.userconsole.web;

import com.grobocop.userconsole.data.TokenEntity;
import com.grobocop.userconsole.security.JwtTokenService;
import com.grobocop.userconsole.web.request.AuthenticationRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthenticationApi {
    private final JwtTokenService jwtTokenService;

    @PostMapping("/login")
    public TokenEntity createToken(@RequestBody AuthenticationRequest authRequest,
                                   HttpServletRequest servletRequest) {
        return jwtTokenService.prepareTokenResponse(authRequest, servletRequest);
    }
}

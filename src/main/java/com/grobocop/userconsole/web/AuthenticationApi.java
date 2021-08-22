package com.grobocop.userconsole.web;

import com.grobocop.userconsole.security.jwt.JwtCreator;
import com.grobocop.userconsole.security.jwt.TokenService;
import com.grobocop.userconsole.web.request.AuthenticationRequest;
import com.grobocop.userconsole.web.response.TokenResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthenticationApi {
    private final JwtCreator jwtCreator;
    private final TokenService tokenService;

    @PostMapping("/login")
    public TokenResponse createToken(@RequestBody AuthenticationRequest authRequest) {
        return jwtCreator.createAccessTokenAndRefreshToken(authRequest.getUsername(), authRequest.getPassword());
    }

    @PostMapping("/refresh")
    public TokenResponse refreshToken(HttpServletRequest request) {
        final Principal userPrincipal = request.getUserPrincipal();
        return jwtCreator.refreshToken(userPrincipal.getName());
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        final Principal userPrincipal = request.getUserPrincipal();
        tokenService.blacklistTokensOfUser(userPrincipal.getName());
        return ResponseEntity.ok().build();
    }
}

package com.grobocop.userconsole.web.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiration;
    private Long refreshTokenExpiration;
}

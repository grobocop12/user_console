package com.grobocop.userconsole.data;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "TOKENS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenEntity {
    @Id
    private String id;
    private String username;
    @Column(columnDefinition = "VARCHAR(500)")
    private String accessToken;
    @Column(columnDefinition = "VARCHAR(500)")
    private String refreshToken;
    private Date issuedAt;
    private Date accessTokenExpiration;
    private Date refreshTokenExpiration;
    private Boolean enabled;
}

package com.grobocop.userconsole.data;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
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
    @GeneratedValue(generator = "token_id_generator")
    @GenericGenerator(name = "token_id_generator", strategy = "sequence")
    private Long id;
    private String username;
    @Column(columnDefinition = "VARCHAR(1000)")
    private String accessToken;
    @Column(columnDefinition = "VARCHAR(1000)")
    private String refreshToken;
    private Date issuedAt;
    private Date accessTokenExpiration;
    private Date refreshTokenExpiration;
    private boolean enabled;
}

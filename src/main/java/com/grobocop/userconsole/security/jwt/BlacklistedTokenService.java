package com.grobocop.userconsole.security.jwt;

import com.grobocop.userconsole.data.TokenEntity;
import com.grobocop.userconsole.data.TokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@AllArgsConstructor
public class BlacklistedTokenService {
    private final TokenRepository tokenRepository;

    public boolean isBlackListed(final String username, final String token) {
        Collection<TokenEntity> allByUsernameAndEnabled = tokenRepository.findAllByUsernameAndEnabled(username, false);
        return allByUsernameAndEnabled.stream().anyMatch(t -> t.getAccessToken().equals(token));
    }
}

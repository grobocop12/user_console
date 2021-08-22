package com.grobocop.userconsole.security.jwt;

import com.grobocop.userconsole.data.TokenEntity;
import com.grobocop.userconsole.data.TokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;

    @Transactional
    public TokenEntity saveNewTokenAndBlacklistOld(final TokenEntity newToken) {
        blacklistTokensOfUser(newToken.getUsername());
        return tokenRepository.save(newToken);
    }

    public void blacklistTokensOfUser(final String username) {
        final List<TokenEntity> tokens = tokenRepository.findAllByUsernameAndEnabled(username, true)
                .stream()
                .peek(t -> t.setEnabled(false))
                .collect(Collectors.toList());
        tokenRepository.saveAll(tokens);
    }

    public boolean isBlackListed(final String username, final String token) {
        Collection<TokenEntity> tokens = tokenRepository.findAllByUsernameAndEnabled(username, false);
        return tokens.stream().anyMatch(t -> t.getAccessToken().equals(token)
                || t.getRefreshToken().equals(token));
    }

    public Collection<TokenEntity> findTokensExpiringBefore(Date date) {
        return tokenRepository.findAllByRefreshTokenExpirationBefore(date);
    }

    public void deleteTokens(Collection<TokenEntity> tokens) {
        tokenRepository.deleteAll(tokens);
    }
}

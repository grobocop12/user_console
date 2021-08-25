package com.grobocop.userconsole.security.jwt;

import com.grobocop.userconsole.data.TokenEntity;
import com.grobocop.userconsole.data.TokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
        disableTokensOfUser(newToken.getUsername());
        return tokenRepository.save(newToken);
    }

    @CacheEvict(value = "blacklist", key = "#username")
    public void disableTokensOfUser(final String username) {
        final List<TokenEntity> tokens = tokenRepository.findAllByUsernameAndEnabled(username, true)
                .stream()
                .peek(t -> t.setEnabled(false))
                .collect(Collectors.toList());
        tokenRepository.saveAll(tokens);
    }

    @Cacheable(value = "blacklist", key = "#username")
    public Collection<TokenEntity> findDisabledTokensOfUser(final String username) {
        return tokenRepository.findAllByUsernameAndEnabled(username, false);
    }

    public Collection<TokenEntity> findTokensExpiringBefore(Date date) {
        return tokenRepository.findAllByRefreshTokenExpirationBefore(date);
    }

    public void deleteTokens(Collection<TokenEntity> tokens) {
        tokenRepository.deleteAll(tokens);
    }

    public boolean checkIfUserHasNonExpiredAccessTokens(String username) {
        return tokenRepository.findAllByUsernameAndEnabled(username, true).size() > 0;
    }
}

package com.grobocop.userconsole.scheduler;

import com.grobocop.userconsole.data.TokenEntity;
import com.grobocop.userconsole.security.jwt.TokenService;
import com.grobocop.userconsole.util.DateAndTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExpiredTokenRemoveScheduler {

    private final TokenService tokenService;
    private final DateAndTimeProvider dateProvider;

    @CacheEvict(value = "blacklist", allEntries = true)
    @Scheduled(cron = "${cron.token.delete}")
    public void deleteExpiredTokens() {
        final Collection<TokenEntity> tokens = tokenService.findTokensExpiringBefore(dateProvider.getCurrentDate());
        tokenService.deleteTokens(tokens);
    }

}

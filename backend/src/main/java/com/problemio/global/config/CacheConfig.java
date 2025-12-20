package com.problemio.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // ranking: 짧은 TTL로 자주 갱신
        CaffeineCache rankingCache = new CaffeineCache(
                "ranking",
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.SECONDS)
                        .maximumSize(500)
                        .build()
        );

        // userDetails: 로그인 시 UserDetails 조회 캐시 (짧은 TTL로 최신성 확보)
        CaffeineCache userDetailsCache = new CaffeineCache(
                "userDetails",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(1_000)
                        .build()
        );

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(rankingCache, userDetailsCache));
        return cacheManager;
    }
}

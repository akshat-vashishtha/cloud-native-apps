package com.learning.gateway.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.time.Duration;

@Configuration
@EnableRedisWebSession(maxInactiveIntervalInSeconds = 28800)
public class SessionConfig {

    private final GatewayAppProperties appProperties;

    public SessionConfig(GatewayAppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName(appProperties.sessionCookieName());
        resolver.addCookieInitializer(builder -> builder
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(Duration.ofHours(8))
        );
        return resolver;
    }
}

package com.learning.gateway.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.HttpStatusReturningServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.server.WebSession;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final GatewayAppProperties appProperties;

    public SecurityConfig(GatewayAppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        RedirectServerAuthenticationSuccessHandler successHandler =
                new RedirectServerAuthenticationSuccessHandler(appProperties.webBaseUrl() + "/");

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/actuator/**",
                                "/api/v1/auth/register",
                                "/oauth2/**",
                                "/login/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2.authenticationSuccessHandler(successHandler))
                .oauth2Client(Customizer.withDefaults())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((exchange, ex) -> {
                            ServerHttpResponse response = exchange.getResponse();
                            response.setStatusCode(HttpStatus.UNAUTHORIZED);
                            return response.setComplete();
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler(spaLogoutSuccessHandler())
                )
                .build();
    }

    private ServerLogoutSuccessHandler spaLogoutSuccessHandler() {
        HttpStatusReturningServerLogoutSuccessHandler statusHandler =
                new HttpStatusReturningServerLogoutSuccessHandler(HttpStatus.OK);
        return (exchange, authentication) ->
                exchange.getExchange().getSession()
                        .flatMap(WebSession::invalidate)
                        .then(statusHandler.onLogoutSuccess(exchange, authentication));
    }
}

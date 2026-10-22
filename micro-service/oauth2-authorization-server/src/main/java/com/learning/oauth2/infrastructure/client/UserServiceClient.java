package com.learning.oauth2.infrastructure.client;

import com.learning.oauth2.infrastructure.config.OAuth2ServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
public class UserServiceClient {

    private final WebClient webClient;
    private final OAuth2ServerProperties properties;

    public UserServiceClient(OAuth2ServerProperties properties, WebClient.Builder webClientBuilder) {
        this.properties = properties;
        this.webClient = webClientBuilder.baseUrl(properties.userServiceUri()).build();
    }

    public Optional<UserAuthDetailsResponse> findByEmail(String email) {
        try {
            UserAuthDetailsResponse body = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/internal/users/by-email")
                            .queryParam("email", email)
                            .build())
                    .header("X-Internal-Api-Key", properties.internalApiKey())
                    .retrieve()
                    .bodyToMono(UserAuthDetailsResponse.class)
                    .block();
            return Optional.ofNullable(body);
        } catch (WebClientResponseException.NotFound ex) {
            return Optional.empty();
        }
    }
}

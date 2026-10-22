package com.learning.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OAuth2AuthorizationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OAuth2AuthorizationServerApplication.class, args);
    }
}

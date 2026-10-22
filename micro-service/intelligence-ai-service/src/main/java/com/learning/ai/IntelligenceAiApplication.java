package com.learning.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IntelligenceAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelligenceAiApplication.class, args);
    }
}

package com.learning.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@ConfigurationPropertiesScan
public class UserRbacServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserRbacServiceApplication.class, args);
    }

}

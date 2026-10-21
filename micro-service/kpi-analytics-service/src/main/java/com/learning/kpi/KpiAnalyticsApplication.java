package com.learning.kpi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KpiAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(KpiAnalyticsApplication.class, args);
    }
}

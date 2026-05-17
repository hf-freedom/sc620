package com.production;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProductionSchedulingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductionSchedulingApplication.class, args);
    }
}

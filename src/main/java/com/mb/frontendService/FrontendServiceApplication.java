package com.mb.frontendService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class FrontendServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(FrontendServiceApplication.class);

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(FrontendServiceApplication.class, args);
    }

}

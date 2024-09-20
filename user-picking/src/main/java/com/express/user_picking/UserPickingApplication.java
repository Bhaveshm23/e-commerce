package com.express.user_picking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserPickingApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserPickingApplication.class, args);
    }

}

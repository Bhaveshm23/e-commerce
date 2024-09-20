package com.express.order_picking_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OrderPickingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderPickingServiceApplication.class, args);
    }

}

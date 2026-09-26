package com.military.assetmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AssetManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssetManagementApplication.class, args);
    }
}

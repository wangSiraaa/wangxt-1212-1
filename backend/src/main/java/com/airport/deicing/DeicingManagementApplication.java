package com.airport.deicing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.airport.deicing.mapper")
@EnableScheduling
public class DeicingManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeicingManagementApplication.class, args);
    }
}

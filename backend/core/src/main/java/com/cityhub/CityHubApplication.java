package com.cityhub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.cityhub.mapper")
@SpringBootApplication
public class CityHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(CityHubApplication.class, args);
    }

}

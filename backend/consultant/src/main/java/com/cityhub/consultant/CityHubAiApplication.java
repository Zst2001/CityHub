package com.cityhub.consultant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com.cityhub.consultant")
@MapperScan("com.cityhub.consultant.mapper")
public class CityHubAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CityHubAiApplication.class, args);
    }

}

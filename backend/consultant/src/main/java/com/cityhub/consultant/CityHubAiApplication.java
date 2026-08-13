package com.cityhub.consultant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(
        scanBasePackages = "com.cityhub.consultant",
        excludeName = {
                "dev.langchain4j.openai.spring.AutoConfig",
                "dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration"
        }
)
public class CityHubAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CityHubAiApplication.class, args);
    }

}

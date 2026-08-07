package com.yasarbilgi.ats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;

@SpringBootApplication(exclude = RedisRepositoriesAutoConfiguration.class)
public class AtsSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtsSystemApplication.class, args);
    }

}

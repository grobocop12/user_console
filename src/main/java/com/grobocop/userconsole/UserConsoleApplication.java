package com.grobocop.userconsole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class UserConsoleApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserConsoleApplication.class, args);
    }

}

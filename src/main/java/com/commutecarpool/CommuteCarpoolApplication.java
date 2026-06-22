package com.commutecarpool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommuteCarpoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommuteCarpoolApplication.class, args);
    }
}

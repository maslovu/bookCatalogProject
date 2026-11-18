package com.maslov.booksmaslov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableCaching
@SpringBootApplication
@EnableRetry
@EnableAsync
public class BooksMaslovApplication {

    public static void main(String[] args) {
        SpringApplication.run(BooksMaslovApplication.class, args);
    }
}

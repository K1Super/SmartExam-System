package com.smartexam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.smartexam.mapper")
@EnableCaching
@EnableScheduling
public class SmartExamApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartExamApplication.class, args);
    }
}

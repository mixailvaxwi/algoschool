package com.algoschool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlgoSchoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlgoSchoolApplication.class, args);
    }

}

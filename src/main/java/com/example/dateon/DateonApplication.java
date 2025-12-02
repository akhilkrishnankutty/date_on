package com.example.dateon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DateonApplication {

	public static void main(String[] args) {
		SpringApplication.run(DateonApplication.class, args);
	}

}

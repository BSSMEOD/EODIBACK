package com.eod.eod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EodApplication {

	public static void main(String[] args) {
		SpringApplication.run(EodApplication.class, args);
	}

}

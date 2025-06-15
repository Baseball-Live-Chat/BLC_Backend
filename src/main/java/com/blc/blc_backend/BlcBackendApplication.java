package com.blc.blc_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlcBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlcBackendApplication.class, args);
	}

}

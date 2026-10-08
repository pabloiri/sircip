package com.cabreras.sircip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SircipApplication {

	public static void main(String[] args) {
		SpringApplication.run(SircipApplication.class, args);
	}

}

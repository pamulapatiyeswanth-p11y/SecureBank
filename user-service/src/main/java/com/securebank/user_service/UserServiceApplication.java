package com.securebank.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
        System.out.println("JWT SECRET AT STARTUP: " +
                System.getProperty("jwt.secret"));
        System.out.println("ENV JWT SECRET: " +
                System.getenv("JWT_SECRET"));
        SpringApplication.run(UserServiceApplication.class, args);
	}

}

package com.team.catchup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CatchupApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatchupApplication.class, args);
	}

}

package com.ems.copilot.emscopilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EmsCopilotBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmsCopilotBackendApplication.class, args);
	}

}

package br.com.fintech_core_api.fintech_core_api;

import br.com.fintech_core_api.Application;
import org.springframework.boot.SpringApplication;

public class TestApplication {

	public static void main(String[] args) {
		SpringApplication.from(Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}

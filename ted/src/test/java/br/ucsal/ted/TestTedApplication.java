package br.ucsal.ted;

import org.springframework.boot.SpringApplication;

public class TestTedApplication {

	public static void main(String[] args) {
		SpringApplication.from(TedApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

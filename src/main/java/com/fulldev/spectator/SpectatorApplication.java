package com.fulldev.spectator;

import com.fulldev.spectator.principal.Principal;
import com.fulldev.spectator.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpectatorApplication implements CommandLineRunner{

	@Autowired
	private SerieRepository repository;
	public static void main(String[] args) {
		SpringApplication.run(SpectatorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal(repository);
		principal.muestraElMenu();
	}
}
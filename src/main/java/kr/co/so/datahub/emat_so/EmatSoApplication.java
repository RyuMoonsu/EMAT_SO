package kr.co.so.datahub.emat_so;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmatSoApplication implements CommandLineRunner {
	private MainService mainService;

	public EmatSoApplication(MainService mainService) {
		this.mainService = mainService;
	}

	public static void main(String[] args) {
		SpringApplication.run(EmatSoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		this.mainService.ready();
		this.mainService.startThread();
	}
}

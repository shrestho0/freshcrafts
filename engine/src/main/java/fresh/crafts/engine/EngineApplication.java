package fresh.crafts.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class EngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(EngineApplication.class, args);
	}

	@PostConstruct
	public void postConstruct() {
		System.out.println("@PostConstruct method called");
	}
}

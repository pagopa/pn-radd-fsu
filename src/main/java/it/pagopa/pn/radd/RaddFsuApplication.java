package it.pagopa.pn.radd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class RaddFsuApplication {

	public static void main(String[] args) {
		SpringApplication.run(RaddFsuApplication.class, args);
	}


	@RestController
	@RequestMapping("/")
	public static class RootController {

		@GetMapping("/")
		public String home() {
			return "";
		}
	}
}


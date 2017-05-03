package am.ik.figlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootApplication
public class FigletServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FigletServiceApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes() {
		FigletHandler handler = new FigletHandler();
		return handler.route();
	}
}

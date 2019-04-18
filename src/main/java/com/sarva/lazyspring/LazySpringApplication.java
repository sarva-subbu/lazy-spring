package com.sarva.lazyspring;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class LazySpringApplication {

	public static void main(String[] args) {
		System.setProperty("spring.main.lazy-initialization", "true");
		SpringApplication.run(LazySpringApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes(LazyService lazyService) {
		return route(GET("/hello"), r -> ServerResponse.ok().syncBody("Hello!!!")).andRoute(GET("/cars/{make}"),
				lazyService::getCarByMake);
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

}

@Service
class LazyService {

	@Autowired
	private RestTemplate restTemplate;

	public Mono<ServerResponse> getCarByMake(ServerRequest serverRequest) {
		Car car = restTemplate
				.getForEntity("http://localhost:8081/cars/" + serverRequest.pathVariable("make"), Car.class).getBody();
		if (null == car) {
			return ServerResponse.ok().syncBody("Not found");
		}
		return ServerResponse.ok().body(BodyInserters.fromObject(car));
	}

}

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class Car {
	private String make;

	private String model;
}

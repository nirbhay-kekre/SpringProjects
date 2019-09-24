package edu.sjsu.cmpe275;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = { "edu.sjsu.cmpe275" })
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}

package com.example.docDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class DocDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocDemoApplication.class, args);
	}

}

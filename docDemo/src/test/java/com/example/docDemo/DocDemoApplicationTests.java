package com.example.docDemo;

import com.example.docDemo.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DocDemoApplicationTests {

	@Autowired
	private DocumentService documentService;

	@Test
	void contextLoads() {
		assertThat(documentService).isNotNull();
	}

}

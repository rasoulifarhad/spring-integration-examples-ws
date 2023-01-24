package com.example.demoSiDsl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest
class DemoSiDslApplicationTests {


	@Autowired
	protected ApplicationContext applicationContext ;
	protected WebTestClient webTestClient;



	@BeforeEach
	public void setup() {
		webTestClient =  WebTestClient.bindToApplicationContext(applicationContext).build();
	}

	@Test
	public void sendPostRequestTests() {

		final FluxExchangeResult<String> theResponseMessage =  webTestClient
				.post()
				.uri("/webflux")
				.accept(MediaType.TEXT_PLAIN)
				.contentType(MediaType.TEXT_PLAIN)
				.header("Remote-Addr", "google.com")
				.body(BodyInserters.fromObject("Hello WebFlux Endpoint, testing!"))
				.exchange()
				.expectStatus()
				.isOk()
				.returnResult(String.class);

		final String theResponsePayload = theResponseMessage.getResponseBody().blockFirst();
		
		assertTrue(theResponsePayload != null && !theResponsePayload.contains("no remote address found") , "The response should contain an address, but it contains: "+ theResponsePayload);

	}

	// @Test
	// void contextLoads() {
	// }

}

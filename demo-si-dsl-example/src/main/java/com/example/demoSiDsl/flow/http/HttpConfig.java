package com.example.demoSiDsl.flow.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.http.dsl.Http;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("HttpConfig")
public class HttpConfig {
    

    @Bean
	public DirectChannel sampleChannel() {
		return new DirectChannel();
	}
    
    @Bean
	public IntegrationFlow inGate() {
		log.info("Initializing inbound gateway...");
		return IntegrationFlows.from(Http.inboundGateway("/checkInbound")
				.requestMapping(m -> m.methods(HttpMethod.POST))
				.mappedRequestHeaders("customHeader")
				.id("idInGate"))
				.enrichHeaders(h -> h.header("header", "inboundHeader"))
				.headerFilter("accept-encoding",false)
				.channel("sampleChannel")
				.get();
	}  
    
    @Bean
	public IntegrationFlow outGate(){
		return IntegrationFlows.from("sampleChannel")
				.handle(Http.outboundGateway("https://catfact.ninja/fact") // {pathParam} appended would consider value from next step
				//.uriVariable("pathParam", "header[customHeader]") // Fetch header value from incoming request and store in pathParam
				.httpMethod(HttpMethod.GET)
				.expectedResponseType(CatFact.class))
				.get(); // replace '.get()' instead of 'logAndReply()' if printing in log is to be avoided 
	}    


    @Data
    public static class CatFact {
	
	private String fact;
	private int length;

    }
}

package com.example.demoSiDsl.flow.http;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.http.dsl.Http;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("EnricherConfig")
public class EnricherConfig {
   
    @Bean
    @Profile("EnricherConfig")
    public RestTemplate restTemplate() {
         return new RestTemplate();
    }

    @Bean
    @Profile("EnricherConfig")
	public IntegrationFlow jsonEnricherFlow(RestTemplate restTemplate) {
		// return IntegrationFlows.from(Function.class)
		return IntegrationFlows.from(EnricherGateway.class)
				.transform(Transformers.fromJson(Map.class))
				.enrich((enricher) -> enricher
						.<Map<String, ?>>requestPayload((message) ->
								((List<?>) message.getPayload().get("attributeIds"))
										.stream()
										.map(Object::toString)
										.collect(Collectors.joining(",")))
						.requestSubFlow((subFlow) ->
								subFlow.handle(
										Http.outboundGateway("/attributes?id={ids}", restTemplate)
												.httpMethod(HttpMethod.GET)
												.expectedResponseType(Map.class)
												.uriVariable("ids", "payload")))
						.propertyExpression("attributes", "payload.attributes"))
				.<Map<String, ?>, Map<String, ?>>transform(
						(payload) -> {
							payload.remove("attributeIds");
							return payload;
						})
				.transform(Transformers.toJson())
				.get();
	}    


    // @Autowired
	public interface  EnricherGateway extends Function<String, String> {
        String apply(String var1);
    }

    @Bean
    @Profile("EnricherConfig")
    @DependsOn("jsonEnricherFlow")
    public Runner runner(EnricherGateway enricherGateway) {
        return new Runner(enricherGateway);
    }
    
    @Profile("EnricherConfig")
    public static class Runner {

        private EnricherGateway enricherGateway;

        public Runner(EnricherGateway enricherGateway) {
            this.enricherGateway = enricherGateway;
        }

        @Scheduled(fixedDelay = 60000)
        public void run() {
            String request =
				"{" +
						"\"name\":\"House\"," +
						"\"attributeIds\": [1,3,5]" +
						"}";

		    // String reply =
			// 	"{\"attributes\": [" +
			// 			"{\"id\": 1, \"value\":\"Waterproof\"}," +
			// 			"{\"id\": 3, \"value\":\"SoundProof\"}," +
			// 			"{\"id\": 5, \"value\":\"Concrete\"}" +
			// 			"]}";
             
            System.out.println("request-> " + request);
            String resultJson = this.enricherGateway.apply(request);
            System.out.println("reply-> " + resultJson);

        }

    }


//     @SpringBootTest
// @AutoConfigureMockRestServiceServer
// @AutoConfigureWebClient(registerRestTemplate = true)
// class SpringIntegrationEnricherApplicationTests {

// 	@Autowired
// 	private MockRestServiceServer mockRestServiceServer;

// 	@Autowired
// 	private Function<String, String> enricherGateway;

// 	@Test
// 	void testJsonEnrichWithHttp() {
// 		String request =
// 				"{" +
// 						"\"name\":\"House\"," +
// 						"\"attributeIds\": [1,3,5]" +
// 						"}";

// 		String reply =
// 				"{\"attributes\": [" +
// 						"{\"id\": 1, \"value\":\"Waterproof\"}," +
// 						"{\"id\": 3, \"value\":\"SoundProof\"}," +
// 						"{\"id\": 5, \"value\":\"Concrete\"}" +
// 						"]}";

// 		this.mockRestServiceServer.expect(requestTo("/attributes?id=1,3,5")).andExpect(method(HttpMethod.GET))
// 				.andRespond(withSuccess(reply, MediaType.APPLICATION_JSON));

// 		String resultJson = this.enricherGateway.apply(request);
// 		this.mockRestServiceServer.verify();

// 		assertThat(resultJson).isEqualTo(
// 				"{" +
// 						"\"name\":\"House\"," +
// 						"\"attributes\":[" +
// 						"{\"id\":1,\"value\":\"Waterproof\"}," +
// 						"{\"id\":3,\"value\":\"SoundProof\"}," +
// 						"{\"id\":5,\"value\":\"Concrete\"}" +
// 						"]}"
// 		);
// 	}

// }
}

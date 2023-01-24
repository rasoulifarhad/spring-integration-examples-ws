package com.example.demospringintegrationkafkadsl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.transformer.ObjectToStringTransformer;
import org.springframework.messaging.MessageChannel;

@Configuration
public class AnotherConf {
    


    // @MessagingGateway(defaultRequestChannel = "requestChannel")
	// public interface Gateway {
	// 	Message<String> sendReceive(Message<String> in);

	// }

    @MessagingGateway(defaultRequestChannel = "requestChannel")
	public interface Gateway {
		String sendReceive(String in);

	}
	@Bean
	TcpNetServerConnectionFactory cf() {
		return new TcpNetServerConnectionFactory(9876);
	}

	@Bean
	TcpInboundGateway tcpGate() {
		TcpInboundGateway gateway = new TcpInboundGateway();
		gateway.setConnectionFactory(cf());
		gateway.setRequestChannel(requestChannel());
		return gateway;
	}

	@Bean
	public HttpRequestHandlingMessagingGateway httpGate() {
		HttpRequestHandlingMessagingGateway gateway = new HttpRequestHandlingMessagingGateway(true);
		gateway.setRequestMapping(mapping());
		gateway.setRequestChannel(requestChannel());
		gateway.setRequestPayloadTypeClass(byte[].class);
		// gateway.setRequestPayloadTypeClass(String.class);
		return gateway;
	}


    @Bean
    public RequestMapping mapping() {
        RequestMapping requestMapping = new RequestMapping();
        requestMapping.setPathPatterns("/inbound");
        requestMapping.setMethods(HttpMethod.POST);
        requestMapping.setConsumes("text/plain");
        requestMapping.setProduces("text/plain");
        return requestMapping;
    }


    // @Bean
    // public IntegrationFlow inbound() {
    //     return IntegrationFlows.from(Http.inboundGateway("/inbound")
    //             .requestMapping(m -> m.methods(HttpMethod.POST))
    //             .requestPayloadType(String.class))
    //         .channel(requestChannel())
    //         // .channel("httpRequest")
    //         .get();
    // }
        
	@Bean
	public MessageChannel requestChannel() {
		return new PublishSubscribeChannel();
	}

	@Bean
	@DependsOn("errorFlow")
	public IntegrationFlow flow() {
		return IntegrationFlows.from("requestChannel")
				.transform(new ObjectToStringTransformer())
				.filter((String p) -> p.startsWith("#spring"),
						f -> f.discardChannel("rejected"))
				.handle(httpOutbound())
				// .transform("payload[0]")
				.transform(new ObjectToStringTransformer())
				.get();
	}

	@Bean
	public IntegrationFlow errorFlow() {
		return IntegrationFlows.from("rejected")
				.transform("'Error: hashtag must start with #spring; got' + payload")
				.get();
	}


    // @ServiceActivator(inputChannel = "httpOutRequest")
    @Bean
    public HttpRequestExecutingMessageHandler httpOutbound() {
        HttpRequestExecutingMessageHandler handler =
            new HttpRequestExecutingMessageHandler("http://localhost:8080/concatIt");
        handler.setHttpMethod(HttpMethod.POST);
        handler.setExpectedResponseType(String.class);
        return handler;
    }    

    // @Bean
    // public IntegrationFlow outbound() {
    //     return IntegrationFlows.from("httpOutRequest")
    //         .handle(Http.outboundGateway("http://localhost:8080/concatIt")
    //             .httpMethod(HttpMethod.POST)
    //             .expectedResponseType(String.class))
    //         .get();
    // }    





    // @Bean
	// public IntegrationFlow replyAndProcessFlow(JmsTemplate jmsTemplate) {
	// 	return IntegrationFlows.from(Http.inboundGateway("/replyAndProcess"))
	// 			.publishSubscribeChannel(publishSubscribeSpec ->
	// 					publishSubscribeSpec.subscribe(flow -> flow
	// 							.transform((payload) -> "OK")
	// 							.enrichHeaders(Collections.singletonMap(HttpHeaders.STATUS_CODE, HttpStatus.ACCEPTED))))
	// 			.<String, String>transform(String::toUpperCase)
	// 			.handle(Jms.outboundAdapter(jmsTemplate).destination("resultQueue"))
	// 			.get();
	// }    



    // import static org.assertj.core.api.Assertions.assertThat;

    // import javax.jms.JMSException;
    // import javax.jms.Message;
    // import javax.jms.TextMessage;
    
    // import org.junit.jupiter.api.Test;
    
    // import org.springframework.beans.factory.annotation.Autowired;
    // import org.springframework.boot.test.context.SpringBootTest;
    // import org.springframework.boot.test.web.client.TestRestTemplate;
    // import org.springframework.integration.dsl.IntegrationFlow;
    // import org.springframework.integration.dsl.context.IntegrationFlowContext;
    // import org.springframework.integration.http.dsl.Http;
    // import org.springframework.jms.core.JmsTemplate;
    // import org.springframework.test.annotation.DirtiesContext;
    
    // @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    // @DirtiesContext
    // class HttpReplyAndProcessApplicationTests {
    
    //     @Autowired
    //     private TestRestTemplate testRestTemplate;
    
    //     @Autowired
    //     private IntegrationFlowContext integrationFlowContext;
    
    //     @Autowired
    //     private JmsTemplate jmsTemplate;
    
    //     @Test
    //     void testReplyAndProcess() throws JMSException {
    
    //         IntegrationFlow clientFlow =
    //                 (flow) -> flow
    //                         .handle(
    //                                 Http.outboundGateway(this.testRestTemplate.getRootUri() + "/replyAndProcess",
    //                                         this.testRestTemplate.getRestTemplate())
    //                                         .expectedResponseType(String.class));
    
    //         IntegrationFlowContext.IntegrationFlowRegistration registration =
    //                 this.integrationFlowContext
    //                         .registration(clientFlow).register();
    
    //         String reply = registration.getMessagingTemplate().convertSendAndReceive("test", String.class);
    //         assertThat(reply).isEqualTo("OK");
    
    //         Message result = this.jmsTemplate.receive("resultQueue");
    
    //         assertThat(result).isNotNull();
    
    //         String payload = ((TextMessage) result).getText();
    //         assertThat(payload).isEqualTo("TEST");
    //     }    
}

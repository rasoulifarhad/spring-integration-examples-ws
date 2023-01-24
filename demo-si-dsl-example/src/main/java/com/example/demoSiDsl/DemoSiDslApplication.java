package com.example.demoSiDsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author Gary Russell
 * @author Artem Bilan
 * @since 4.3
 */
// @EnableWebMvc  // or @EnableWebFlux
// @EnableWebSecurity  // or @EnableWebFluxSecurity
@SpringBootApplication
@EnableIntegration
@IntegrationComponentScan
@EnableScheduling
@EnableIntegrationManagement
// @EnableIntegrationGraphController
@EnableIntegrationGraphController(allowedOrigins="*") 
// @EnableIntegrationGraphController(path = "/testIntegration", allowedOrigins="http://localhost:9090")// 
//@EnableConfigurationProperties(KafkaAppProperties.class)
@EnableWebFlux
public class DemoSiDslApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoSiDslApplication.class, args);
	}


	// @Override
    // protected void configure(HttpSecurity http) throws Exception {
	//     http
    //         .authorizeRequests()
    //            .antMatchers("/testIntegration/**").hasRole("ADMIN")
    //         // ...
    //         .formLogin();
    // }	

	// the model includes the type attribute. The possible types are:

    // input: Identifies the direction from MessageChannel to the endpoint, inputChannel, or requestChannel property

    // output: The direction from the MessageHandler, MessageProducer, or SourcePollingChannelAdapter to the MessageChannel through an outputChannel or replyChannel property

    // error: From MessageHandler on PollingConsumer or MessageProducer or SourcePollingChannelAdapter to the MessageChannel through an errorChannel property;

    // discard: From DiscardingMessageHandler (such as MessageFilter) to the MessageChannel through an errorChannel property.

    // route: From AbstractMappingMessageRouter (such as HeaderValueRouter) to the MessageChannel. Similar to output but determined at run-time. May be a configured channel mapping or a dynamically resolved channel. Routers typically retain only up to 100 dynamic routes for this purpose, but you can modify this value by setting the dynamicChannelLimit property.


}

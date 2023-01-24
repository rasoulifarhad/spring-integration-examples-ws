package com.example.demospringintegrationkafkadsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;

    /**
	 * @author Gary Russell
	 * @author Artem Bilan
	 * @since 4.3
	 */
	@SpringBootApplication
	@EnableIntegration
	@IntegrationComponentScan
	// @EnableConfigurationProperties(KafkaAppProperties.class)
	public class DemoSpringIntegrationKafkaDslApplication {

		public static void main(String[] args) {
			SpringApplication.run(DemoSpringIntegrationKafkaDslApplication.class, args);
		}
	
		
}

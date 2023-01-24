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
	
	
		// public static void main(String[] args) throws Exception {
		// 	ConfigurableApplicationContext context =
		// 			new SpringApplicationBuilder(DemoSpringIntegrationKafkaDslApplication.class)
		// 			.web(WebApplicationType.NONE)
		// 			.run(args);
		// 	context.getBean(DemoSpringIntegrationKafkaDslApplication.class).runDemo(context);
		// 	context.close();
		// }
	
		// private void runDemo(ConfigurableApplicationContext context) {
		// 	KafkaGateway kafkaGateway = context.getBean(KafkaGateway.class);
		// 	System.out.println("Sending 10 messages...");
		// 	for (int i = 0; i < 10; i++) {
		// 		String message = "foo" + i;
		// 		System.out.println("Send to Kafka: " + message);
		// 		kafkaGateway.sendToKafka(message, this.properties.getTopic());
		// 	}
	
		// 	for (int i = 0; i < 10; i++) {
		// 		Message<?> received = kafkaGateway.receiveFromKafka();
		// 		System.out.println(received);
		// 	}
		// 	System.out.println("Adding an adapter for a second topic and sending 10 messages...");
		// 	addAnotherListenerForTopics(this.properties.getNewTopic());
		// 	for (int i = 0; i < 10; i++) {
		// 		String message = "bar" + i;
		// 		System.out.println("Send to Kafka: " + message);
		// 		kafkaGateway.sendToKafka(message, this.properties.getNewTopic());
		// 	}
		// 	for (int i = 0; i < 10; i++) {
		// 		Message<?> received = kafkaGateway.receiveFromKafka();
		// 		System.out.println(received);
		// 	}
		// 	context.close();
		// }
	
		// @Autowired
		// private KafkaAppProperties properties;
	
		
		// @Autowired
		// private IntegrationFlowContext flowContext;
	
		// @Autowired
		// private KafkaProperties kafkaProperties;
	
		// public void addAnotherListenerForTopics(String... topics) {
		// 	Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
		// 	// change the group id so we don't revoke the other partitions.
		// 	consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG,
		// 			consumerProperties.get(ConsumerConfig.GROUP_ID_CONFIG) + "x");
		// 	IntegrationFlow flow =
		// 		IntegrationFlows
		// 			.from(Kafka.messageDrivenChannelAdapter(
		// 					new DefaultKafkaConsumerFactory<String, String>(consumerProperties), topics))
		// 			.channel("fromKafka")
		// 			.get();
		// 	this.flowContext.registration(flow).register();
		// }
	
}

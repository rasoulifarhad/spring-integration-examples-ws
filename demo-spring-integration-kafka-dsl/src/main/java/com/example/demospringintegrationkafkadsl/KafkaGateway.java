package com.example.demospringintegrationkafkadsl;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.Message;


@MessagingGateway
public interface KafkaGateway {
	
			@Gateway(requestChannel = "toKafka.input")
			void sendToKafka(String payload, @Header(KafkaHeaders.TOPIC) String topic);
	
			@Gateway(replyChannel = "fromKafka", replyTimeout = 10000)
			Message<String> receiveFromKafka();
	
}

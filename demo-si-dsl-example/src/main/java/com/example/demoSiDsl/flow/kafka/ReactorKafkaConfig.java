package com.example.demoSiDsl.flow.kafka;

import java.util.Collections;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.Scheduled;

import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

@Configuration
@Profile("ReactorKafkaConfig")
public class ReactorKafkaConfig {
    
    @Autowired
    KafkaProperties kafkaProperties;

    @Bean
    @Profile("ReactorKafkaConfig")
    public ReactiveKafkaProducerTemplate<String,String>  reactiveKafkaProducerTemplate() {

        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(kafkaProperties.buildProducerProperties()));
    }

    @ServiceActivator(inputChannel = "kafkaProducerChannel",outputChannel = "nullChannel")
    @Profile("ReactorKafkaConfig")
    public Mono<?>  sendToKafkaReactively(String payload) {
        return reactiveKafkaProducerTemplate().send("testTopic", payload);

    }

    @Bean
    @Profile("ReactorKafkaConfig")
    public ReactiveKafkaConsumerTemplate<String,String>  reactiveKafkaConsumerTemplate() {

        return new ReactiveKafkaConsumerTemplate<>(
                       ReceiverOptions.<String,String>create(kafkaProperties.buildConsumerProperties())
                                        .subscription(Collections.singleton("testTopic"))
                       );
    }

    @Bean
    @Profile("ReactorKafkaConfig")
    public IntegrationFlow kafkaConsumerFlow(ReactiveKafkaConsumerTemplate<String, String> reactiveKafkaConsumerTemplate) {

        return IntegrationFlows.from(reactiveKafkaConsumerTemplate.receiveAutoAck().map(GenericMessage::new))
                            .<ConsumerRecord<String, String>, String>transform(ConsumerRecord::value)
                            .log(LoggingHandler.Level.DEBUG, "com.example.spring.integration.reactor.kafka")
                            .<String,String>transform(String::toUpperCase)
                            // .channel(c -> c.queue("resultChannel"))
                            .channel(resultChannel())
                            .get();


    }


    @Bean
    @Profile("ReactorKafkaConfig")
	public MessageChannel kafkaProducerChannel() {
        return new  DirectChannel();
    }

    @Bean
    @Profile("ReactorKafkaConfig")
	public PollableChannel resultChannel() {
        return new  QueueChannel();
    }

    @Bean
    @Profile("ReactorKafkaConfig")
    @DependsOn("kafkaConsumerFlow")
    public Runner runner() {
        return new Runner(kafkaProducerChannel(),resultChannel());
    }
    
    @Profile("ReactorKafkaConfig")
    public static class Runner {

        private final MessageChannel kafkaProducerChannel;
        private final PollableChannel resultChannel;

        public Runner(MessageChannel kafkaProducerChannel,PollableChannel resultChannel) {
            this.kafkaProducerChannel = kafkaProducerChannel;
            this.resultChannel= resultChannel;
        }

        @Scheduled(fixedDelay = 60000)
        public void run() {

            for (int i = 0; i < 100; i++) {
                this.kafkaProducerChannel.send(new GenericMessage<>("test#" + i));
                System.out.println("test#" + i + "  sended");
            }
            for (int i = 0; i < 100; i++) {
                Message<?> message = this.resultChannel.receive(10000);
                String res = (String) message.getPayload();
                System.out.println(res + "  received");

            }
        }

    }


}

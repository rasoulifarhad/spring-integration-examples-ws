package com.example.demoSiDsl.serviceActivator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("MyConfig11")
public class MyConfig11 {

    @Component
    @Profile("MyConfig11")
    public static class ConsumerEndpoint1 {

        @ServiceActivator(inputChannel = "pubSubChannel")
        public Message<String> consumMessage(Message<String> message) {
            log.info("ConsumerEndpoint1 -> Received message from gateway : " + message.getPayload());
            return MessageBuilder.withPayload("message '" + message.getPayload() +"'  received by ConsumerEndpoint1")
                        .build();

        }
    }

    @Component
    @Profile("MyConfig11")
    public static class ConsumerEndpoint2 {

        @ServiceActivator(inputChannel = "pubSubChannel")
        public Message<String> consumMessage(Message<String> message) {
            log.info("ConsumerEndpoint2 -> Received message from gateway : " + message.getPayload());
            return MessageBuilder.withPayload("message '" + message.getPayload() +"'  received by ConsumerEndpoint2")
                        .build();

        }
    }

    @Component
    @Profile("MyConfig11")
    public static class ConsumerEndpoint3 {

        @ServiceActivator(inputChannel = "pubSubChannel")
        public Message<String> consumMessage(Message<String> message) {
            log.info("ConsumerEndpoint3 -> Received message from gateway : " + message.getPayload());
            return MessageBuilder.withPayload("message '" + message.getPayload() +"'  received by ConsumerEndpoint3")
                        .build();

        }
    }

    @MessagingGateway(name = "myGateway" , defaultRequestChannel = "pubSubChannel")
    @Profile("MyConfig11")
    public interface CustomGageway {

        @Gateway(requestChannel = "pubSubChannel")
        public Message<String>  print(Message<String> msg) ;

    }

    @Configuration
    @Profile("MyConfig11")
    public static class App {

        // @Autowired
        // @Qualifier("pubSubChannel")
        // private PublishSubscribeChannel pubSubChannel ;

        @Autowired
        private CustomGageway customGageway ;

        @Bean(name = "pubSubChannel")
        public PublishSubscribeChannel channel1() {
            return new PublishSubscribeChannel();
        }

        @Bean 
        public ApplicationRunner runnerDemo() {
            return (args) -> {
                for (int i = 0; i < 10; i++) {
                    Message<String> msg = MessageBuilder.withPayload("Msg " + i).build();
                    Message<String> res = customGageway.print(msg);
                    log.info(res.getPayload());
                }
            };
        } 
    }
}

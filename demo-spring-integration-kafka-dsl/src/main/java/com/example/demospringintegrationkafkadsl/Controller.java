package com.example.demospringintegrationkafkadsl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableConfigurationProperties(KafkaAppProperties.class)
public class Controller {

    @Autowired
    KafkaGateway kafkaGateway;

    @Autowired
    private KafkaAppProperties kafkaAppProperties;


    AtomicInteger atomicInteger = new AtomicInteger(0);
    AtomicInteger anotherAtomicInteger = new AtomicInteger(0);

    @ResponseBody
	@PostMapping(value="/uppercase",produces=MediaType.APPLICATION_JSON_VALUE,consumes=MediaType.APPLICATION_JSON_VALUE)
	public String uppercaseRequest(@RequestBody String request) throws InterruptedException, ExecutionException, TimeoutException {

        System.out.println("Sending message...");

        String message = request + atomicInteger.addAndGet(1);
        System.out.println("Send to Kafka: " + message);
        kafkaGateway.sendToKafka(message, kafkaAppProperties.getTopic());

        Message<String> received = kafkaGateway.receiveFromKafka();
        System.out.println(received);

        return received.getPayload() ;
   
	}

    // @ResponseBody
	// @PostMapping(value="/uppercase",produces=MediaType.APPLICATION_JSON_VALUE,consumes=MediaType.APPLICATION_JSON_VALUE)
	// public String anotherUppercaseRequest(@RequestBody String request) throws InterruptedException, ExecutionException, TimeoutException {

    //     System.out.println("Sending message...");

    //     String message = request + anotherAtomicInteger.addAndGet(1);
    //     System.out.println("Send to Kafka: " + message);
    //     kafkaGateway.sendToKafka(message, properties.getNewTopic());

    //     Message<String> received = kafkaGateway.receiveFromKafka();
    //     System.out.println(received);

    //     return received.getPayload() ;
   
	// }



}

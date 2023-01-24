package com.example.demospringintegrationkafkadsl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {



    @ResponseBody
	@PostMapping(value="/concatIt",produces=MediaType.TEXT_PLAIN_VALUE,consumes=MediaType.TEXT_PLAIN_VALUE)
	public String concatIt(@RequestBody String request) throws InterruptedException, ExecutionException, TimeoutException {

        System.out.println("Sending message..." + request);


        String message = request + "_"  + request ;
        return message ;
   
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

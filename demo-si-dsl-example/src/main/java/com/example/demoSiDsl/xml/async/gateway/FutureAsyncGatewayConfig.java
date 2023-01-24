package com.example.demoSiDsl.xml.async.gateway;

// import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;



import lombok.extern.slf4j.Slf4j;

@Profile("FutureAsyncGatewayConfig")
@Configuration
@ImportResource({ "classpath:/META-INF/com/example/demoSiDsl/xml/async/gateway/math-service-config.xml" })
@Slf4j
public class FutureAsyncGatewayConfig {
    
    private static ExecutorService executor = Executors.newFixedThreadPool(100);
    private static int timeout = 20;

    public interface MathServiceGateway {
        Future<Integer> multiplyByTwo(int i);
    }

    public static class MathService {

        private final Random random = new Random();

	    public int multiplyByTwo(int i) throws Exception{
		    long sleep = random.nextInt(10) * 100;
		    Thread.sleep(sleep);
		        return i*2;
	    }
    }


    @Autowired
    MathServiceGateway mathService    ;
    
    // @Autowired
    // private Gt100Filter gt100Filter;

        
    @Bean
    @Profile("FutureAsyncGatewayConfig")
    @Order(2)
    public ApplicationRunner runWait() {
        return (args) -> {

            try {
                Thread.sleep(100); // NOSONAR magic number
            }
            catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e1);
            }
    
        };
    }



    @Bean
    @Profile("FutureAsyncGatewayConfig")
    @Order(10)
    public ApplicationRunner runAsyncGateway() throws Exception {
        return (args) ->  {
            
            Map<Integer, Future<Integer>> results = new ConcurrentHashMap<Integer, Future<Integer>>();
            Random random = new Random();
            for (int i = 0; i < 100; i++) {
                int number = random.nextInt(200);
                Future<Integer> result = mathService.multiplyByTwo(number);
                results.put(number, result);
            }
            for (final Map.Entry<Integer, Future<Integer>> resultEntry : results.entrySet()) {
                executor.execute(() -> {
                    int[] result = processFuture(resultEntry);
                    if(result != null) {
                        if (result[1] == -1){
                            log.info("Multiplying " + result[0] + " should be easy. You should be able to multiply any number < 100 by 2 in your head");
                        } else if (result[1] == -2){
                            log.info("Multiplication of " + result[0] + " by 2 is can not be accomplished in " + timeout + " seconds");
                        } else {
                            log.info("Result of multiplication of " + result[0] + " by 2 is " + result[1]);
                        }
                   } 
                });
            }
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.MINUTES);
            log.info("Finished");
    
        }; 
    } 

    public  int[] processFuture(Map.Entry<Integer, Future<Integer>> resultEntry){
        int originalNumber = resultEntry.getKey();
        Future<Integer> result = resultEntry.getValue();
        log.info("--------processFuture for "+ originalNumber);
        try {
            int finalResult =  result.get(timeout, TimeUnit.SECONDS);
            return new int[]{originalNumber, finalResult};
        } catch (ExecutionException e) {
            return new int[]{originalNumber, -1};
        } catch (TimeoutException tex){
            return new int[]{originalNumber, -2};
        } catch (Exception ex){
            ex.printStackTrace();
            // ignore
        }
        return null;
    }
    

}

    // Async Gateway Sample

    // Gateways provide a convenient way to expose a Proxy over a service-interface thus 
    // giving you POJO-based access to a messaging system (based on objects in your own 
    // domain, or primitives/Strings, etc). However, when you invoke a method, you 
    // expect the method to return. A gateway's method call represents a contract with 
    // the messaging system, which states that for each request, there will always be a 
    // reply. Therefore you must always guarantee that your message flow is in compliance 
    // with such a contract.
    
    // But what about the cases where you can't (e.g, message was filtered out and discarded 
    // or routed into a unidirectional sub-flow)?
    
    // Starting with Spring Integration 2.0, we introduced support for an Asynchronous 
    // Gateway, which is a convenient way to initiate flows, where you may not know, if 
    // a reply is expected or how long will it take for it to arrive. A natural way to 
    // handle these types of scenarios in Java would be to rely upon java.util.concurrent.Future 
    // instances. That is exactly what Spring Integration uses to support Asynchronous 
    // Gateways.
    
    // This example demonstrates how you can apply an Asynchronous Gateway based on the 
    // following simple use case:
    
    // We are sending a request to a MathService to multiply random numbers by 2. As you 
    // can see from the configuration there is a filter that discards any request for 
    // the number that is less then a 100. This means that there will be no replies 
    // coming for the requests with numbers less then 100. Typically, when using the 
    // regular Gateway, the gateway method would lock until a timeout occurs. In this 
    // example, however, the responses are coming back right away as Java Futures which 
    // we evaluate.
    
    // To run this sample, simply execute org.springframework.integration.samples.async.gateway.AsyncGatewayTest.
    
    // You should see the following output:
    
    // INFO : org.springframework.integration.samples.async.gateway.AsyncGatewayTest - Result of multiplication of 107 by 2 is 214
    // INFO : org.springframework.integration.samples.async.gateway.AsyncGatewayTest - Result of multiplication of 146 by 2 is 292
    // INFO : org.springframework.integration.samples.async.gateway.AsyncGatewayTest - Result of multiplication of 189 by 2 is 378
    // INFO : org.springframework.integration.samples.async.gateway.AsyncGatewayTest - Result of multiplication of 130 by 2 is 260
    // . . . . .
    // INFO : org.springframework.integration.samples.async.gateway.AsyncGatewayTest - Multiplication of 38 by 2 is can not be accomplished in 20 seconds
    // INFO : org.springframework.integration.samples.async.gateway.AsyncGatewayTest - Multiplication of 39 by 2 is can not be accomplished in 20 seconds
    // INFO : org.springframework.integration.samples.async.gateway.AsyncGatewayTest - Multiplication of 36 by 2 is can not be accomplished in 20 seconds
    // INFO : org.springframework.integration.samples.async.gateway.AsyncGatewayTest - Multiplication of 37 by 2 is can not be accomplished in 20 seconds
    
    // Spring Integration 4.0 provided the capability to more easily configure Messaging 
    // Gateways with Java configuration.
    
    // Spring Integration 4.1 added support for ListenableFuture and Promise (from project reactor) 
    // return types.
    
    // The ListenableFutureTest and PromiseTest test classes replicate the above test 
    // case, using those return types, and showing the use of @MessagingGateway java 
    // configuration.


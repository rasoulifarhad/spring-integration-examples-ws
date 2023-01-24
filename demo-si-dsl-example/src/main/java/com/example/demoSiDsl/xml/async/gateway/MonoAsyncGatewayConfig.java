package com.example.demoSiDsl.xml.async.gateway;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.MessagingGateway;


import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Profile("MonoAsyncGatewayConfig")
@Configuration
public class MonoAsyncGatewayConfig {
    


    @Autowired
    private MonoMathGateway gateway;

    
    // @Autowired
    // private MathService mathService;

    // @Autowired
    // private Gt100Filter gt100Filter;

    public static class MathService {

        private final Random random = new Random();

	    public int multiplyByTwo(int i) throws Exception{
		    long sleep = random.nextInt(10) * 100;
		    Thread.sleep(sleep);
		        return i*2;
	    }
    }

    @MessageEndpoint
    @Profile("MonoAsyncGatewayConfig")
    public static class Gt100Filter {

        @Filter(inputChannel="gatewayChannel", outputChannel="mathServiceChannel")
        public boolean filter(int i) {
            return i > 100;
        }

    }



    @Profile("MonoAsyncGatewayConfig")
    @MessagingGateway(defaultReplyTimeout = "0")
    public interface MonoMathGateway {

        @Gateway(requestChannel = "gatewayChannel")
        Mono<Integer> multiplyByTwo(int number);

    }


    @Bean
    @Profile("MonoAsyncGatewayConfig")
    @Order(20)
    public ApplicationRunner runAsyncGatewayMonoGateway() throws Exception {
        return (args) -> {
            Random random = new Random();
            int[] numbers = new int[100];
            int expectedResults = 0;
            for (int i = 0; i < 100; i++) {
                numbers[i] = random.nextInt(200);
                if (numbers[i] > 100) {
                    expectedResults++;
                }
            }
            final CountDownLatch latch = new CountDownLatch(expectedResults);
            final AtomicInteger failures = new AtomicInteger();
            for (int i = 0; i < 100; i++) {
                final int number = numbers[i];
                gateway.multiplyByTwo(number)
                        .subscribeOn(Schedulers.boundedElastic())
                        .filter(p -> p != null)
                        .doOnNext(result1 -> {
                            log.info("Result of multiplication of " + number + " by 2 is " + result1);
                            latch.countDown();
                        })
                        .doOnError(t -> {
                            failures.incrementAndGet();
                            log.error("Unexpected exception for " + number, t);
                        }).subscribe();
            }
            // assertTrue(latch.await(60, TimeUnit.SECONDS));
            // assertThat(failures.get(), greaterThanOrEqualTo(0));
            log.info("Finished");
    
        };
    }
    
}



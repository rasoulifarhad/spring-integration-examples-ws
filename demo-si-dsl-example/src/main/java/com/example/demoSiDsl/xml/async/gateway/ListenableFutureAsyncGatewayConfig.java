package com.example.demoSiDsl.xml.async.gateway;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.persistence.Basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import ch.qos.logback.core.rolling.helper.IntegerTokenConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("ListenableFutureAsyncGatewayConfig")
@Configuration
public class ListenableFutureAsyncGatewayConfig {
    
    // @Autowired
    // private MathGateway gateway;

    // @Autowired
    // private Gt100Filter gt100Filter;

    // @Autowired
    // MathServiceGateway mathService    ;

    @MessageEndpoint
    @Profile("ListenableFutureAsyncGatewayConfig")
    public static class MathService {

        private final Random random = new Random();

        @ServiceActivator(inputChannel="mathServiceChannel")
	    public Integer multiplyByTwo(Integer i) throws Exception{
		    long sleep = random.nextInt(10) * 100;
		    Thread.sleep(sleep);
		        return i*2;
	    }

    }

    
    @MessageEndpoint
    @Profile("ListenableFutureAsyncGatewayConfig")
    public static class Gt100Filter {

        @Filter(inputChannel="gatewayChannel", outputChannel="mathServiceChannel"
                           ,discardChannel = "errorChannel")
        public boolean filter(Integer i ) {
            return false;
        }


    }


    // @MessageEndpoint
    // @Profile("TcpAnnotationBaseConfig")
    // public static class Echo {

    //     @Transformer(inputChannel = "fromTcp",outputChannel = "toEcho")
    //     public Integer convert(byte[] bytes) {
    //         return  ByteBuffer.wrap(bytes).getInt();
    //     }

    //     @Transformer(inputChannel = "resultToString")
    //     public String convertResult(byte[] bytes) {

    //         return new String(bytes);

    //     }

    // }
  

    @Profile("ListenableFutureAsyncGatewayConfig")
    @MessagingGateway(defaultReplyTimeout = "0", asyncExecutor = "exec")
    public interface MathGateway {

        @Gateway(requestChannel = "gatewayChannel")
        ListenableFuture<Integer> multiplyByTwo(Integer number);

    }
    
    @Bean
    @Profile("ListenableFutureAsyncGatewayConfig")
    public MessageChannel gatewayChannel() {
        DirectChannel d=   new DirectChannel();
        d.setDatatypes(Integer.class);
        return d;
    }

    @Bean
    @Profile("ListenableFutureAsyncGatewayConfig")
    public MessageChannel mathServiceChannel() {
        DirectChannel d=   new DirectChannel();
        d.setDatatypes(Integer.class);
        return d;
    }



    
    @Bean
    @Profile("ListenableFutureAsyncGatewayConfig")
    public AsyncTaskExecutor exec() {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setThreadNamePrefix("exec-");
        return simpleAsyncTaskExecutor;
    }




    @Bean
    @Profile("ListenableFutureAsyncGatewayConfig")
    @Order(10)
    public ApplicationRunner runAsyncGatewayListenableFuture(MathGateway mathGateway) throws Exception {

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
                ListenableFuture<Integer> result = mathGateway.multiplyByTwo(number);
                ListenableFutureCallback<Integer> callback = new ListenableFutureCallback<Integer>() {
    
                    @Override
                    public void onSuccess(Integer result) {
                        log.info("Result of multiplication of " + number + " by 2 is " + result);
                        latch.countDown();
                    }
    
                    @Override
                    public void onFailure(Throwable t) {
                        failures.incrementAndGet();
                        log.error("Unexpected exception for " + number, t);
                        latch.countDown();
                    }
                };
                result.addCallback(callback);
            }
            // assertTrue(latch.await(60, TimeUnit.SECONDS));
            // assertEquals(0, failures.get());
            log.info("Finished");
            };
    }


}



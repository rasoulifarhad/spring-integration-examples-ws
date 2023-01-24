package com.example.demoSiDsl.flow.basic;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("MyConfigurations")
public class MyConfiguration {
 
    //@Bean
    public AtomicInteger integerSource() {
        return new AtomicInteger();
    }


    //@Bean
    public IntegrationFlow myFlow() {
        log.info("init flow");

        return IntegrationFlows.fromSupplier(integerSource()::getAndIncrement,
                                    t -> t.poller(Pollers.fixedRate(100)))
                        .channel("inputChannel")
                        .filter((Integer p) ->  p > 0)
                        .transform(Object::toString)
                        .intercept()
                        .channel(MessageChannels.queue())
                        .get();

    }

    

    // @Bean
    // @IntegrationConverter
    // public Converter<byte[],Integer>  bytesToIntegerConverter() {
    //     SimpleMessageConverter  f;
    //     return (byteArray) -> new Integer(1);
    // }
}

package com.example.demoSiDsl.flow.function;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@Profile("FunctionConfig")
public class FunctionConfig {
    
    

    // @MessagingGateway
    @Profile("FunctionConfig")
    public interface MessageFunction extends Function<Message<String> , Message<String>> {

    }

    // @Bean
    // @Profile("FunctionConfig")
    // MessageChannel myIntegrationServiceChannel() {
    //     return new DirectChannel() ;
    // }

    @Bean
    @Profile("FunctionConfig")
    public IntegrationFlow uppercaseFlow() {
        return IntegrationFlows.from(MessageFunction.class )
                                .<String,String>transform(String::toUpperCase)               
                                .get();
    }

    @Bean
    @DependsOn("uppercaseFlow")
    @Profile("FunctionConfig")
    public Runner runner(MessageFunction gateway) {
        return new Runner(gateway);
    }
    
    public static class Runner {

        private final MessageFunction gateway;

        public Runner(MessageFunction gateway) {
            this.gateway = gateway;
        }

        @Scheduled(fixedDelay = 5000)
        public void run() {

            Message<String> message =    gateway.apply(MessageBuilder.withPayload("foo").build());
            System.out.println( "foo -> "  + message.getPayload());
        }

    }
    // @Bean
    // @Profile("FunctionConfig")
    // public IntegrationFlow uppercaseFlow() {
    //     return IntegrationFlows.from(Function.class, 
    //                                         (gateway) -> gateway.beanName("uppercase") )
    //                             .<String,String>transform(String::toUpperCase)               
    //                             .get();
    // }


}

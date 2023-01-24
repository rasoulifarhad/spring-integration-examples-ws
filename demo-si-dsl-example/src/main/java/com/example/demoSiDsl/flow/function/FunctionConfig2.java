package com.example.demoSiDsl.flow.function;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

@Configuration
@Profile("FunctionConfig2")
public class FunctionConfig2 {

    @Bean 
    @Profile("FunctionConfig2")
    public Function<String,String> toUpperCaseFunction() {

        return String::toUpperCase;
    }

    @Bean
    @Profile("FunctionConfig2")
    public Supplier<String> stringSupplier() {
        return () -> "foo";
    }

    @Bean
    @Profile("FunctionConfig2")
    public Consumer<String> stringConsumer() {
        return (t) -> System.out.println(t); 
    }



    @Bean
    @Profile("FunctionConfig2")
    public IntegrationFlow supplierFlow() {

        return IntegrationFlows.fromSupplier(stringSupplier())
                        .transform(toUpperCaseFunction())
                        .channel("suppliedChannel")
                        .handle(stringConsumer())
                        .get();
    }
    
}

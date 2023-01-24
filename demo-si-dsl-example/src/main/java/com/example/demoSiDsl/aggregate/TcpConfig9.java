package com.example.demoSiDsl.aggregate;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.scheduling.annotation.Scheduled;

import net.datafaker.Faker;

import lombok.extern.slf4j.Slf4j;
 
@Slf4j
@Configuration
@Profile("TcpConfig9")
public class TcpConfig9 {
    

    @MessagingGateway
    @Profile("TcpConfig9")
    public interface Upcase {

        @Gateway(requestChannel = "upcase.input")
        List<String>  upcase(List<String> strings) ;

    }

    @Bean
    public IntegrationFlow upcase() {
        return (f) ->  f
                    .split()
                    .<String,String>transform(String::toUpperCase) 
                    .aggregate();
    }


    @Bean
    @DependsOn("upcase")
    public Runner runner(Upcase upcaseGate) {
        return new Runner(upcaseGate);
    }

    public static class Runner {

        private final Upcase upcaseGate;

        public Runner(Upcase upcaseGate) {
            this.upcaseGate = upcaseGate;
        }

        private List<String> names() {
            Faker faker = new Faker();
            List<String> names = new ArrayList<>(100);
            for (int i = 0 ; i<100 ; i++) {

                names.add(faker.name().name());

            }
            return names ;

        }
        @Scheduled(fixedDelay = 60000)
        @Profile("TcpConfig9")
        public void run() {
            List<String> replies = this.upcaseGate.upcase(names()); // null for timeout
            replies.forEach((r) -> log.info(r));

            // System.out.println("Received:" + reply);
        }

    }

}

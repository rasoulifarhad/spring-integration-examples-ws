package com.example.demoSiDsl.flow.tcp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

// Client/Server Example using Java DSL
@Slf4j
@Configuration
@Profile("TcpConfig7")
public class TcpConfig7 {

    @Bean
    public IntegrationFlow server() {
        return IntegrationFlows.from(Tcp.inboundGateway(Tcp.netServer(1234)))
                .transform(Transformers.objectToString())
                .log()
                .handle((p, h) -> "OK")
                .get();
    }
    //get the reply from the Gate method...
    @Bean
    public IntegrationFlow client() {
        return IntegrationFlows.from(Gate.class)
                .handle(Tcp.outboundGateway(Tcp.netClient("localhost", 1234)))
                .transform(Transformers.objectToString())
                .get();
    }

    @Bean
    @DependsOn("client")
    public Runner runner(Gate gateway) {
        return new Runner(gateway);
    }

    public static class Runner {

        private final Gate gateway;

        public Runner(Gate gateway) {
            this.gateway = gateway;
        }

        @Scheduled(fixedDelay = 60000)
        public void run() {
            String reply = this.gateway.sendAndReceive("foo"); // null for timeout
            log.info("Received:" + reply);
            // System.out.println("Received:" + reply);
        }

    }

    public interface Gate {

        @Gateway(replyTimeout = 5000)
        String sendAndReceive(String out);

    }
}

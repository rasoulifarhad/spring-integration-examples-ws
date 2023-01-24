package com.example.demoSiDsl.flow.tcp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

// Client/Server Example using Java DSL
@Slf4j
@Configuration
@Profile("TcpConfig6")
public class TcpConfig6 {

    @Bean
    public IntegrationFlow server() {
        return IntegrationFlows.from(Tcp.inboundGateway(Tcp.netServer(1234)))
                .transform(Transformers.objectToString())
                .log()
                .handle((p, h) -> "OK")
                .get();
    }
    
    @Bean
    public IntegrationFlow client() {
        return IntegrationFlows.from(Gate.class )
                .handle(Tcp.outboundGateway(Tcp.netClient("localhost", 1234)))
                .transform(Transformers.objectToString())
                .handle((p, h) -> {
                    System.out.println("Received:" + p);
                    return null;
                })
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

        @Scheduled(fixedDelay = 5000)
        public void run() {

            this.gateway.send("foo");
            log.debug("foo sended");
        }

    }

    public interface Gate {

        void send(String out);

    }
   
}

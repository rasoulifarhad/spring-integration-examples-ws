package com.example.demoSiDsl.flow.tcp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;

import lombok.extern.slf4j.Slf4j;

// Client/Server Example using Java DSL
@Slf4j
@Configuration
@Profile("TcpConfig8")
public class TcpConfig8 {

    @Bean
    public IntegrationFlow server() {
        log.info("init server");
        return IntegrationFlows.from(Tcp.inboundGateway(Tcp.netServer(1234)).id("inGate"))
                .transform(Transformers.objectToString())
                .log()
                .handle((p, h) -> "OK")
                .get();
    }
    //An alternative, for a single bean for the client...
    @Bean
    public IntegrationFlow client() {
        log.info("init client");

        return IntegrationFlows.fromSupplier(
                        () -> "foo", 
                        e -> e.poller(Pollers.fixedRate(60000)).id("fooPooler")
                    )
            .handle(Tcp.outboundGateway(Tcp.netClient("localhost", 1234).id("clientOutGate")))
            .transform(Transformers.objectToString())
            .handle((p, h) -> {
                System.out.println("Received:" + p);
                return null;
            })
            .get();
    }


}

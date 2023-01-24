package com.example.demoSiDsl.flow.tcp;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.TcpCodecs;

import lombok.extern.slf4j.Slf4j;

// Client/Server Example using Java DSL

@Slf4j
@Configuration
@Profile("TcpConfig5")
public class TcpConfig5 {

    @Bean
    public IntegrationFlow server() {
        return IntegrationFlows.from(Tcp.inboundGateway(
                    Tcp.netServer(1234)
                        .serializer(codec()) // default is CRLF
                        .deserializer(codec()))) // default is CRLF
                .transform(Transformers.objectToString()) // byte[] -> String
                .<String, String>transform(p -> p.toUpperCase())
                .get();
    }

    @Bean
    public IntegrationFlow client() {
        return IntegrationFlows.from(MyGateway.class)
                .handle(Tcp.outboundGateway(
                    Tcp.netClient("localhost", 1234)
                        .serializer(codec()) // default is CRLF
                        .deserializer(codec()))) // default is CRLF
                .transform(Transformers.objectToString()) // byte[] -> String
                .get();
    }

    @Bean
    public AbstractByteArraySerializer codec() {
        return TcpCodecs.lf();
    }

    @Bean
    @DependsOn("client")
    ApplicationRunner runner(MyGateway gateway) {
        return args -> {
            log.info(gateway.exchange("foo"));
            log.info(gateway.exchange("bar"));
            // System.out.println(gateway.exchange("foo"));
            // System.out.println(gateway.exchange("bar"));
        };
    }

    public interface MyGateway {

        String exchange(String out);

    }
   
}

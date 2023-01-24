package com.example.demoSiDsl.annotation.tcp;

import net.datafaker.Faker;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@Profile("TcpAnnotationBaseConfig")
public class TcpAnnotationBaseConfig {

    @MessagingGateway(defaultRequestChannel = "toTcp")
    @Profile("TcpAnnotationBaseConfig")
    public interface Gateway {
        String viaTcp(String in);
    }

    @Bean
    @ServiceActivator(inputChannel = "toTcp")
    public MessageHandler tcpOutGate(AbstractClientConnectionFactory connectionFactory) {
     
        TcpOutboundGateway gate = new TcpOutboundGateway();
        gate.setConnectionFactory(connectionFactory);
        gate.setOutputChannelName("resultToString");
        return gate;

    }
    
    @Bean
    public TcpInboundGateway tcpInGate(AbstractServerConnectionFactory connectionFactory) {

        TcpInboundGateway gate = new TcpInboundGateway();
        gate.setConnectionFactory(connectionFactory);
        gate.setRequestChannel(fromTcp());
        return gate;

    }

    @Bean
    public MessageChannel fromTcp() {
        return new DirectChannel();
    }

    @Bean
    public AbstractServerConnectionFactory serverCF() {
        return new  TcpNetServerConnectionFactory(1234);

    }

    @Bean
    public AbstractClientConnectionFactory clientCF() {
        return new TcpNetClientConnectionFactory("localhost", 1234);
    }

    @MessageEndpoint
    @Profile("TcpAnnotationBaseConfig")
    public static class Echo {

        @Transformer(inputChannel = "fromTcp",outputChannel = "toEcho")
        public String convert(byte[] bytes) {
            return new String(bytes);
        }

        @ServiceActivator(inputChannel = "toEcho")
        public String upcase(String in) {
            return in.toUpperCase();
        }

        @Transformer(inputChannel = "resultToString")
        public String convertResult(byte[] bytes) {

            return new String(bytes);

        }

    }

    @Bean
    @DependsOn("tcpOutGate")
    public Runner runner(Gateway gateway) {
        return new Runner(gateway);
    }
    
    public static class Runner {

        private final Gateway gateway;
        private Faker faker = new Faker();
        public Runner(Gateway gateway) {
            this.gateway = gateway;
        }

        @Scheduled(fixedDelay = 5000)
        public void run() {
            String in =  faker.name().name() ; 
            String res = this.gateway.viaTcp(in);
            log.info("'" + in + "' sended " + "and '" + res + "' received");
        }

    }

}

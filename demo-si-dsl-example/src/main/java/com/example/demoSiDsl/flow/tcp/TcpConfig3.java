package com.example.demoSiDsl.flow.tcp;

import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("TcpConfig3")
public class TcpConfig3 {
    


    // @Bean
    // public IntegrationFlow tcpServer1234() {
    //     log.info("init server");

    //     return IntegrationFlows.from(Tcp.inboundGateway(Tcp.netServer(1234)
    //                                                         // .deserializer(TcpCodecs.lengthHeader1())
    //                                                         // .serializer(TcpCodecs.lengthHeader1())
    //                                                         // .backlog(30)
    //                                                     )
    //                                     // .errorChannel("tcpIn.errorChannel")
    //                                     // .id("tcpIn")
    //                                     // .requestChannel("tcpChannel")
    //                                     // .replyChannel("replyChannel")
    //                             )
    //                 .transform(Transformers.objectToString("UTF-8"))
    //                 // .log()
    //                 .channel("subscribersFlow.input")
    //                 // .handle((payload, headers) -> "OK")
    //                 .get();
    // }
    
    // @Bean
    // public IntegrationFlow server1235() {
    //     log.info("init server");
    //     return IntegrationFlows.from(Tcp.inboundGateway(Tcp.netServer(1235)).id("inGate1235"))
    //             .transform(Transformers.objectToString())
    //             .log()
    //             .channel("subscribersFlow.input")
    //             // .handle((p, h) -> "OK")
    //             .get();
    // }    

    @Bean
    public IntegrationFlow server() {
        log.info("init server");
        return IntegrationFlows.from(Tcp.inboundGateway(Tcp.netServer(1234)).id("inGate"))
                .transform(Transformers.objectToString())
                .log()
                .channel(subscribersFlowInput())
                // .handle((p, h) -> "OK")
                .get();
    }

    @Bean
    public MessageChannel  subscribersFlowInput() {
        return MessageChannels.queue("subscribersFlowInput",20).get() ;
    }

    @Bean
    public IntegrationFlow subscribersFlow() {
        return flow -> flow
                // .gateway(tcpServer1234())
                .channel(subscribersFlowInput())
                .publishSubscribeChannel(Executors.newCachedThreadPool(), s -> s
                        .subscribe(f -> f
                                .<String>handle((p, h) -> p + "_/_" + "2" )
                                .channel(c -> c.queue("subscriber1Results")))
                        .subscribe(f -> f
                                .<String>handle((p, h) -> p + "_*_" + "2" )
                                .channel(c -> c.queue("subscriber2Results"))))
                .<String>handle((p, h) -> p + "_*_" + "3")
                .channel(c -> c.queue("subscriber3Results"));
    }   


    // @Bean
    // public IntegrationFlow subscribersFlow() {
    //     return flow -> flow
    //             // .gateway(tcpServer1234())
    //             .channel("subscribersFlowInput")
    //             .publishSubscribeChannel(Executors.newCachedThreadPool(), s -> s
    //                     .subscribe(f -> f
    //                             .<Integer>handle((p, h) -> p / 2)
    //                             .channel(c -> c.queue("subscriber1Results")))
    //                     .subscribe(f -> f
    //                             .<Integer>handle((p, h) -> p * 2)
    //                             .channel(c -> c.queue("subscriber2Results"))))
    //             .<Integer>handle((p, h) -> p * 3)
    //             .channel(c -> c.queue("subscriber3Results"));
    // }   

    
    // @Bean
    // public IntegrationFlow client() {
    //     log.info("init client");

    //     return IntegrationFlows.fromSupplier(
    //                     () -> "foo", 
    //                     e -> e.poller(Pollers.fixedRate(60000)).id("fooPooler")
    //                 )
    //         .handle(Tcp.outboundGateway(Tcp.netClient("localhost", 1234).id("clientOutGate")))
    //         .transform(Transformers.objectToString())
    //         // .handle((p, h) -> {
    //         //     System.out.println("Received:" + p);
    //         //     return null;
    //         // })
    //         .get();
    // }
    
    @Bean
    public IntegrationFlow clientGate() {
        log.info("init client");

        return IntegrationFlows.from(Gate.class)
            .handle(Tcp.outboundGateway(Tcp.netClient("localhost", 1234).id("clientOutGateClass")))
            .transform(Transformers.objectToString())
            .get();
    }

    @Bean
    @DependsOn("clientGate")
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
    // @Bean
	// public IntegrationFlow subscriber1ResultsFlow() {
	// 	return IntegrationFlows.from("subscriber1Results")
	// 			.transform("from subscriber1Results: hashtag must start with #spring; got' + payload")
	// 			.get();
	// }
    // @Bean
	// public IntegrationFlow subscriber2ResultsFlow() {
	// 	return IntegrationFlows.from("subscriber2Results")
	// 			.transform("from subscriber2Results: hashtag must start with #spring; got' + payload")
	// 			.get();
	// }
    // @Bean
	// public IntegrationFlow subscriber3ResultsFlow() {
	// 	return IntegrationFlows.from("subscriber3Results")
	// 			.transform("from subscriber3Results: hashtag must start with #spring; got' + payload")
	// 			.get();
	// }


    // @Bean
    // public QueueChannelSpec wrongMessagesChannel() {
    //     return MessageChannels
    //             .queue()
    //             .wireTap("wrongMessagesWireTapChannel");
    // }
    
    // @Bean
    // public IntegrationFlow xpathFlow(MessageChannel wrongMessagesChannel) {
    //     return IntegrationFlows.from("inputChannel")
    //             .filter(new StringValueTestXPathMessageSelector("namespace-uri(/*)", "my:namespace"),
    //                     e -> e.discardChannel(wrongMessagesChannel))
    //             .log(LoggingHandler.Level.ERROR, "test.category", m -> m.getHeaders().getId())
    //             .route(xpathRouter(wrongMessagesChannel))
    //             .get();
    // }
    
    // @Bean
    // public AbstractMappingMessageRouter xpathRouter(MessageChannel wrongMessagesChannel) {
    //     XPathRouter router = new XPathRouter("local-name(/*)");
    //     router.setEvaluateAsString(true);
    //     router.setResolutionRequired(false);
    //     router.setDefaultOutputChannel(wrongMessagesChannel);
    //     router.setChannelMapping("Tags", "splittingChannel");
    //     router.setChannelMapping("Tag", "receivedChannel");
    //     return router;
    // }    


    // 
    // IntegrationFlow as a Gateway

    // The IntegrationFlow can start from the service interface that provides a GatewayProxyFactoryBean component, as the following example shows:
    
    // public interface ControlBusGateway {
    
    //     void send(String command);
    // }
    
    // ...
    
    // @Bean
    // public IntegrationFlow controlBusFlow() {
    //     return IntegrationFlows.from(ControlBusGateway.class)
    //             .controlBus()
    //             .get();
    // }


    // All the proxy for interface methods are supplied with the channel to send messages to the next integration component in the IntegrationFlow. You can mark 
    // the service interface with the @MessagingGateway annotation and mark the methods with the @Gateway annotations. Nevertheless, the requestChannel 
    // is ignored and overridden with that internal channel for the next component in the IntegrationFlow. Otherwise, creating such a configuration by 
    // using IntegrationFlow does not make sense.

    // By default a GatewayProxyFactoryBean gets a conventional bean name, such as [FLOW_BEAN_NAME.gateway]. You can change that ID by using the 
    // @MessagingGateway.name() attribute or the overloaded IntegrationFlows.from(Class<?> serviceInterface, Consumer<GatewayProxySpec> endpointConfigurer) 
    // factory method. Also all the attributes from the @MessagingGateway annotation on the interface are applied to the target GatewayProxyFactoryBean. When
    //  annotation configuration is not applicable, the Consumer<GatewayProxySpec> variant can be used for providing appropriate option for the target proxy. 
    // This DSL method is available starting with version 5.2.
    
    // With Java 8, you can even create an integration gateway with the java.util.function interfaces, as the following example shows:
    
    // @Bean
    // public IntegrationFlow errorRecovererFlow() {
    //     return IntegrationFlows.from(Function.class, (gateway) -> gateway.beanName("errorRecovererFunction"))
    //             .handle((GenericHandler<?>) (p, h) -> {
    //                 throw new RuntimeException("intentional");
    //             }, e -> e.advice(retryAdvice()))
    //             .get();
    // }
    
    // That errorRecovererFlow can be used as follows:
    
    // @Autowired
    // @Qualifier("errorRecovererFunction")
    // private Function<String, String> errorRecovererFlowGateway;
    
        
    
}

package com.example.demoSiDsl.flow.tcp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.serializer.TcpCodecs;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("TcpConfig2")
public class TcpConfig2 {
    

@Bean
public IntegrationFlow tcpServer() {
    log.info("init server");

    return IntegrationFlows.from(Tcp.inboundGateway(Tcp.netServer(1234)
                                                        .deserializer(TcpCodecs.lengthHeader1())
                                                        .serializer(TcpCodecs.lengthHeader1())
                                                        // .backlog(30)
                                                    )
                                    // .errorChannel("tcpIn.errorChannel")
                                    // .id("tcpIn")
                                    // .requestChannel("tcpChannel")
                                    // .replyChannel("replyChannel")
                            )
                // .log(LoggingHandler.Level.INFO, "test.category", m -> m.getPayload())           
                .transform(Transformers.objectToString("UTF-8"))
                // .log(LoggingHandler.Level.INFO, "test.category2", m -> m.getPayload())           
                // .channel("requestChannel")
                .handle((payload, headers) -> "OK")
                .get();
}


//@Bean 
public IntegrationFlow doubleTcpServer() {
    return IntegrationFlows.from(Tcp.inboundGateway(Tcp.netServer(9091)))
                    .<Integer>handle((p, h) -> p * 2  )
                    .get();
}

//@Bean
public IntegrationFlow anotherDoubleTcpServer()  {
    return IntegrationFlows.from(Tcp.inboundGateway(Tcp.netServer(9092)))
                    // .<byte[],String>transform(Object::toString  )
                    .handle(Integer.class, (payload, headers) -> payload * 2)
                    .get();
}
// @Bean
// public IntegrationFlow client() {
//     return f -> f.handle(Tcp.outboundGateway(Tcp.nioClient("localhost", 1234)
//                         .deserializer(TcpCodecs.lengthHeader1())
//                         .serializer(TcpCodecs.lengthHeader1())));
// }

// @Bean
// AbstractServerConnectionFactory server() {
//     return Tcp.netServer(1234)
//             .deserializer(TcpCodecs.lf())
//             .serializer(TcpCodecs.lf())
//             .get();
// }

// @Bean
// AbstractClientConnectionFactory client() {
//     return Tcp.netClient(localhost, 1235)
//             .deserializer(TcpCodecs.lf())
//             .serializer(TcpCodecs.lf())
//             .get();
// }
}


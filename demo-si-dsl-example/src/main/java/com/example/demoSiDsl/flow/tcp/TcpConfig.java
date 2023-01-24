package com.example.demoSiDsl.flow.tcp;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.messaging.MessageChannel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("TcpConfig")
public class TcpConfig {
    
    // @MessagingGateway(defaultRequestChannel = "requestChannel")
	public interface Gateway {
		String sendReceive(String in);

	}    
    // @Bean
    // public MessageChannel tcpIn() {
    //     return new DirectChannel();
    // }    
    // @Bean
	public MessageChannel requestChannel() {
		return new PublishSubscribeChannel();
	}

    // @Bean
	TcpNetServerConnectionFactory cf() {
		return new TcpNetServerConnectionFactory(9876);
	}    
    // @Bean
	TcpInboundGateway tcpGate() {
		TcpInboundGateway gateway = new TcpInboundGateway();
		gateway.setConnectionFactory(cf());
		gateway.setRequestChannel(requestChannel());
        gateway.setErrorChannelName("tcpIn.errorChannel");
        
		return gateway;
	}

    // @Bean
    // @DependsOn("errorFlow")
    public IntegrationFlow tcpServer() {
        return IntegrationFlows.from("requestChannel")
                    .transform(Transformers.objectToString("UTF-8"))
                    .log()
                    // .channel("requestChannel")
                    .handle((payload, headers) -> "OK")
                
                    .get();
    }
    
    // @Bean
	public IntegrationFlow errorFlow() {
		return IntegrationFlows.from("tcpIn.errorChannel")
				.transform("'Error: hashtag must start with #spring; got' + payload")
				.get();
	}
    // @Bean
	//@DependsOn("tcpServer")
	public IntegrationFlow flow() {
		return IntegrationFlows.from("requestChannel")
				.transform(Transformers.objectToString("UTF-8"))
				
				.handle((payload, headers) -> "OK")
				// .transform(new ObjectToStringTransformer())
				.get();
	}    

    // @Bean
    // public IntegrationFlow tcpServer() {
    //     return IntegrationFlows.from(Tcp.inboundGateway(Tcp.netServer(9876)
    //                                                         .deserializer(TcpCodecs.lengthHeader1())
    //                                                         .serializer(TcpCodecs.lengthHeader1())
    //                                                         .backlog(30)
    //                                                     )
    //                                     .errorChannel("tcpIn.errorChannel")
    //                                     .id("tcpIn")
    //                                     // .requestChannel("tcpChannel")
    //                                     // .replyChannel("replyChannel")
    //                             )
    //                 .transform(Transformers.objectToString("UTF-8"))
    //                 .log()
    //                 .channel("requestChannel")
    //                 // .handle((payload, headers) -> "OK")
    //                 .get();
    // }


    //@Bean 
    public IntegrationFlow doubleTcpServer() {
        log.info("init server");

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

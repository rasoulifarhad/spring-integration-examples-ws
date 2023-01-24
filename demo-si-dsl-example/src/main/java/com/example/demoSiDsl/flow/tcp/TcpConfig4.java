package com.example.demoSiDsl.flow.tcp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.dsl.TcpInboundGatewaySpec;
import org.springframework.integration.ip.dsl.TcpServerConnectionFactorySpec;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.messaging.MessageHeaders;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("TcpConfig4")
public class TcpConfig4 {

    //@Value("${socket.port}")
    private int serverSocketPort=1234;
    
    @Bean
    public IntegrationFlow server(ServerSocketHandler serverSocketHandler) {
        log.info("init server");

        TcpServerConnectionFactorySpec connectionFactory = 
            Tcp.netServer(serverSocketPort) 
              .deserializer(ByteArrayCrLfSerializer.INSTANCE)
              .serializer(ByteArrayCrLfSerializer.INSTANCE)
            //   .deserializer(new ByteArrayLfSerializer())
            //   .serializer(new ByteArrayLfSerializer())

              .soTcpNoDelay(true);

        TcpInboundGatewaySpec inboundGateway = 
           Tcp.inboundGateway(connectionFactory);

        return IntegrationFlows
         .from(inboundGateway)
         .handle(serverSocketHandler::handleMessage)
         .get();
    }

    @Bean
    public ServerSocketHandler serverSocketHandler() {
        return new ServerSocketHandler();
    }
    // By default the connection factory is configured to require the input to be terminated by CRLF (e.g. Telnet). 
    //And here is a version that works with just LF (e.g. netcat):
    //@Bean
    //public IntegrationFlow server(ServerSocketHandler serverSocketHandler) {
    //   return IntegrationFlows.from(Tcp.inboundGateway(
    //        Tcp.netServer(1234)
    //            .deserializer(TcpCodecs.lf())
    //            .serializer(TcpCodecs.lf())))
    //        .handle(serverSocketHandler::handleMessage)
    //        .get();
    //}
    //nc localhost 1234
    //foo
    //FOO
    //^C

   @Slf4j
   public static class ServerSocketHandler {
   
      public String handleMessage(byte[] message, MessageHeaders messageHeaders) {
          String string = new String(message);
          log.info(string);
        //   System.out.println(string);
          return string.toUpperCase();
      }
      // telnet localhost 1234
      //public String handleMessage(Message<?> message, MessageHeaders messageHeaders) {
      //    log.info(message.getPayload());
      //    return message.getPayload().toString();
      //} 
   }
   
    
}

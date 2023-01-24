package com.example.demoSiDsl.xml.tcp.multiplexing;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.MessageTimeoutException;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

@Configuration
@ImportResource({ "classpath:/META-INF/com/example/demoSiDsl/xml/tcp/multiplexing/tcpClientServerDemo-conversion-context.xml" })
@Profile("TcpClientServerMultiplexConfig")
public class TcpClientServerMultiplexConfig {
    
    public interface SimpleGateway {

        String send(String text);
    
    } 

    

    /**
     * Simple service that receives data in a byte array,
     * converts it to a String and appends it with ':echo'.
     *
     * @author Gary Russell
     * @since 2.1
     *
     */
    public static class EchoService {
    
        public String test(String input) throws InterruptedException {
            if ("FAIL".equals(input)) {
                throw new RuntimeException("Failure Demonstration");
            }
            else if(input.startsWith("TIMEOUT_TEST")) {
                Thread.sleep(3000);
            }
    
            return input + ":echo";
        }
    
        public MessageTimeoutException noResponse(String input) {
            if ("TIMEOUT_TEST_THROW".equals(input)) {
                throw new MessageTimeoutException("No response received for " + input);
            }
            else {
                return new MessageTimeoutException("No response received for " + input);
            }
        }
    
    }    


    
    /**
     * Simple byte array to String converter; allowing the character set
     * to be specified.
     *
     * @author Gary Russell
     * @since 2.1
     *
     */
    public static class ByteArrayToStringConverter implements Converter<byte[], String> {
    
        private String charSet = "UTF-8";
    
        public String convert(byte[] bytes) {
            try {
                return new String(bytes, this.charSet);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return new String(bytes);
            }
        }
    
        /**
         * @return the charSet
         */
        public String getCharSet() {
            return charSet;
        }
    
        /**
         * @param charSet the charSet to set
         */
        public void setCharSet(String charSet) {
            this.charSet = charSet;
        }
    
    }    

    // @Autowired
    // AbstractServerConnectionFactory crLfServer;

    // @Autowired
    // AbstractClientConnectionFactory client;

    // @Autowired
    // @Qualifier("outAdapter.client")
    // AbstractEndpoint outAdapterClient;

    // @Autowired
    // @Qualifier("inAdapter.client")
    // AbstractEndpoint inAdapterClient;


    // @EventListener(ApplicationReadyEvent.class)
    // public void setup() {
    //     if (!this.outAdapterClient.isRunning()) {
    //         this.outAdapterClient.start();
    //     }
    //     if (!this.inAdapterClient.isRunning()) {
    //         this.inAdapterClient.start();
    //     }
    // }


    @Slf4j
    public static class  run {
        
        @Autowired
        SimpleGateway gateway;
    
        @Autowired
        AbstractServerConnectionFactory crLfServer;
    
        @Autowired
        AbstractClientConnectionFactory client;
    
        @Autowired
        @Qualifier("outAdapter.client")
        AbstractEndpoint outAdapterClient;
    
        @Autowired
        @Qualifier("inAdapter.client")
        AbstractEndpoint inAdapterClient;
    
        @Bean
        //@DependsOn("client")
        @Profile("TcpClientServerMultiplexConfig")
        @Order(2)
        public ApplicationRunner runWait() {
            return (args) -> {

                try {
                    Thread.sleep(100); // NOSONAR magic number
                }
                catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e1);
                }
        
            };
        }
    
        @Bean
        //@DependsOn("client")
        @Profile("TcpClientServerMultiplexConfig")
        @Order(5)
        public ApplicationRunner runSetup() {
            return (args) -> {

                if (!outAdapterClient.isRunning()) {
                    client.setPort(crLfServer.getPort());
                    outAdapterClient.start();
                    inAdapterClient.start();

                }
            };
        }
    
        @Bean
        //@DependsOn("client")
        @Profile("TcpClientServerMultiplexConfig")
        @Order(10)
        ApplicationRunner runHappyDay(SimpleGateway gw) {

            return (args) -> {
                String sendStr = "999Hello world!";
                String result = gw.send(sendStr); // first 3 bytes is correlationid
                log.info("send: '" + sendStr + "' receive: '"+ result+ "'");
            };

        }        

        @Bean
        //@DependsOn("client")
        @Profile("TcpClientServerMultiplexConfig")
        @Order(20)
        ApplicationRunner runMultiPlex(SimpleGateway gw) {
            return (args) -> {

                TaskExecutor executor = new SimpleAsyncTaskExecutor();
                for (int i = 100; i < 200; i++) {
                    final int j = i;
                    executor.execute(() -> {
                        String sendStr = j + "Hello world!";
                        String result = gw.send(sendStr); // first 3 bytes is correlationId
                        log.info("send: '" + sendStr + "' receive: '"+ result+ "'");
                    });
                }
            };
    
        }        

        @Bean
        //@DependsOn("client")
        @Profile("TcpClientServerMultiplexConfig")
        @Order(30)
        ApplicationRunner runTimeoutThrow(SimpleGateway gw) {
            return (args) -> {
                try {
                    String sendStr = "TIMEOUT_TEST_THROW";
                    String result = gw.send(sendStr); // first 3 bytes is correlationid
                    log.info(result);
                } catch (Exception e){
                    log.info("-----TIMEOUT_TEST_THROW",e.getCause());

                }
            };

        }        

        @Bean
        //@DependsOn("client")
        @Profile("TcpClientServerMultiplexConfig")
        @Order(40)
        ApplicationRunner runTimeoutReturn(SimpleGateway gw) {
            return (args) -> {
                try {
                    String sendStr = "TIMEOUT_TEST_RETURN";
                    String result = gw.send(sendStr); // first 3 bytes is correlationid
                    log.info(result);
                } catch (Exception e){
                    log.info("-----TTIMEOUT_TEST_RETURN",e.getCause());

                }
            };

        }        
    }

    // TCP Client-Server Multiplex Sample

    // If this is your first experience with the spring-integration-ip module, start with the tcp-client-server 
    // project in the basic folder.
    
    // That project uses outbound and inbound tcp gateways for communication. As discussed in the Spring Integration 
    // Reference Manual, this has some limitations for performance. If a shared socket (single-use="false") is used, 
    // only one message can be processed at a time (on the client side); we must wait for the response to the current 
    // request before we can send the next request. Otherwise, because only the payload is sent over tcp, the framework 
    // cannot correlate responses to requests.
    
    // An alternative is to use a new socket for each message, but this comes with a performance overhead. The solution 
    // is to use Collaborating Channel Adapters (see SI Reference Manual). In such a scenario, we can send multiple 
    // requests before a response is received. This is termed multiplexing.
    
    // This sample demonstrates how to configure collaborating channel adapters, on both the client and server sides, 
    // and one technique for correlating the responses to the corresponding request.
    
    // gateway -> outbound-channel-adapter
    //         |-> aggregator
    
    // inbound-channel-adapter->aggregator->transformer
    
    // When the aggregator receives the reply, the group is released and transformed to just the reply which is then 
    // returned to the gateway.
    
    // Unlike when using TCP gateways, there is no way to communicate an IO error to the waiting thread, which is 
    // sitting in the initial <gateway/> waiting for a reply - it "knows" nothing about the downstream flow, such 
    // as a read timeout on the socket.
    
    // This sample now shows how to use the group-timeout on the aggregator to release the group under this condition. 
    // Further, it routes the discarded message to a service activator which return a MessagingTimeoutException which 
    // is routed to the waiting thread and thrown to the caller.
    
    // gateway -> outbound-channel-adapter
    //         |-> aggregator
    
    // aggregator(group-timeout discard)->service-activator
    
    // A service activator is used here instead of a transformer because you may wish to take some other action when 
    // the timeout condition occurs.
    
    // There are two test cases, one throws an exception; the other returns one as the message payload.
    
    // When the payload of the reply messsage is a Throwable normal gateway processing detects that and throws it to 
    // the caller.
    
    // Similarly, when the async flow throws an exception, it is wrapped in an ErrorMessage and routed to the caller.
    
    // Thus, this shows both techniques for returning an exception to a gateway caller, even when the messaging is 
    // entirely asynchronous.    

}

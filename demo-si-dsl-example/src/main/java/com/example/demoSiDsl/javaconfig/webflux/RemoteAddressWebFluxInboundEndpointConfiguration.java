package com.example.demoSiDsl.javaconfig.webflux;

import java.net.InetSocketAddress;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.integration.webflux.inbound.WebFluxInboundEndpoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Configuration for the application trying out the webflux inbound endpoint that
 * stores the remote address of the client sending request to the endpoint in a HTTP
 * header.
 * Note that if this would be the configuration for a test and the inbound endpoint
 * started as part of that test, then no real server (e.g. Netty) would not be started
 * and there would be no remote address set on requests.
 *
 * @author Ivan Krizsan
 */

 @Slf4j
@Configuration
@Profile("WebfluxInboundConfig")
public class RemoteAddressWebFluxInboundEndpointConfiguration {
    /* Constant(s): */
    public static final String INBOUND_ENDPOINT_REQUEST_CHANNEL= "webfluxInboundEndpointRequestChannel";
    public static final String INBOUND_ENDPOINT_PATH_PATTERN = "/webflux";

    /**
     * Creates and configures the webflux inbound endpoint.
     *
     * @param inInboundHeaderMapper Mapper to be set on the inbound endpoint.
     * The mapper will map HTTP headers to message headers in requests and message headers
     * to HTTP headers in responses.
     * @return Inbound endpoint bean.
     */
    @Bean
    @Profile("WebfluxInboundConfig")
    WebFluxInboundEndpoint webfluxInboundEndpoint(
        final HeaderMapper<HttpHeaders> inInboundHeaderMapper) {
        final WebFluxInboundEndpoint theInboundEndpoint = new RemoteExtractingWebFluxInboundEndpoint();

        final RequestMapping theRequestMapping = new RequestMapping();
        /* Accept only POST requests. */
        theRequestMapping.setMethods(HttpMethod.POST);
        /* Accept all media types in requests. */
        theRequestMapping.setConsumes(MediaType.ALL_VALUE);
        /* Produces plaintext responses. */
        theRequestMapping.setProduces(MediaType.TEXT_PLAIN_VALUE);
        /* HTTP path pattern at which the endpoint will listen. */
        theRequestMapping.setPathPatterns(INBOUND_ENDPOINT_PATH_PATTERN);

        theInboundEndpoint.setRequestMapping(theRequestMapping);
        theInboundEndpoint.setRequestPayloadTypeClass(String.class);
        theInboundEndpoint.setHeaderMapper(inInboundHeaderMapper);
        theInboundEndpoint.setRequestChannelName(INBOUND_ENDPOINT_REQUEST_CHANNEL);

        return theInboundEndpoint;
    }

    /**
     * Creates the header mapper that will map HTTP headers to message headers
     * for inbound requests.
     * Note that the HTTP header containing the remote client address must be added to
     * the names of inbound HTTP headers to be mapped to message headers, otherwise it will not
     * appear in request messages.
     *
     * @return HTTP header mapper for the webflux inbound endpoint.
     */
    @Bean
    @Profile("WebfluxInboundConfig")
    HeaderMapper<HttpHeaders> requestHeaderMapper() {
        final DefaultHttpHeaderMapper theHttpHeaderMapper = DefaultHttpHeaderMapper.inboundMapper();
        final String[] theInboundHeaderNames = new String[] {
            DefaultHttpHeaderMapper.HTTP_REQUEST_HEADER_NAME_PATTERN,
            RemoteExtractingWebFluxInboundEndpoint.REMOTE_ADDRESS_HEADER
        };
        theHttpHeaderMapper.setInboundHeaderNames(theInboundHeaderNames);
        return theHttpHeaderMapper;
    }

    /**
     * Handles requests received by the inbound endpoint during tests.
     * If there is a remote address header in the request, then the value of this header
     * is to become the payload of the response message.
     * Otherwise a message saying that no remote address header present in request will
     * be the response payload.
     *
     * @param inRequestMessage Request message.
     * @return Response message.
     */
    @ServiceActivator(inputChannel = INBOUND_ENDPOINT_REQUEST_CHANNEL)
    @Profile("WebfluxInboundConfig")
    public Message<String> handleRequest(final Message<String> inRequestMessage) {
        String theResponsePayload = "Uh-oh, no remote address found!";

        log.info("WebFlux inbound endpoint received a request message: {}",inRequestMessage);

        final Object theRemoteHeaderValue = inRequestMessage.getHeaders().get( RemoteExtractingWebFluxInboundEndpoint.REMOTE_ADDRESS_HEADER);
        if (theRemoteHeaderValue != null) {
            theResponsePayload = theRemoteHeaderValue.toString();

            log.info("Message header in request contained the remote client address: {}", theRemoteHeaderValue);
        } else {
            log.warn("No message header containing the client remote address found in request!");
        }

        return MessageBuilder
                    .withPayload(theResponsePayload)
                    /*
                    * All headers from the request are copied to the response. No need to remove
                    * any headers as the HTTP header mapper will take care of not mapping any message
                    * headers that it does not have in its list of outbound header names to HTTP headers.
                    */
                    .copyHeaders(inRequestMessage.getHeaders())
                    .build();
    }

        /**
         * WebFlux inbound endpoint that inserts the remote address of a client into
         * a HTTP header in the request, when one is available.
         *
         * @author Ivan Krizsan
         */
        public static class RemoteExtractingWebFluxInboundEndpoint extends WebFluxInboundEndpoint  {

            /* Constant(s): */
            /** Default remote address header. */
            public final static String REMOTE_ADDRESS_HEADER = "Remote-Addr";
        
            /* Instance variable(s): */
            protected String mRemoteAddressHeader = REMOTE_ADDRESS_HEADER;
        
            /**
             * Handles a request.
             * This method stores the remote address of the client having sent the request in
             * a HTTP header in the request if the remote address is available.
             * Further processing of requests is delegated to the superclass.
             *
             * @param inServerWebExchange Current server exchange.
             * @return To indicate when request handling is complete.
             */
            @Override
            public Mono<Void> handle(final ServerWebExchange inServerWebExchange) {
                ServerWebExchange theServerWebExchange = inServerWebExchange;
        
                final InetSocketAddress theRemoteAddress = inServerWebExchange
                    .getRequest()
                    .getRemoteAddress();
        
                /* Only insert the remote address header if a remote address is indeed available. */
                if (theRemoteAddress != null) {
                    /*
                     * Need to create a mutable wrapper around the ServerHttpRequest
                     * since the original ServerHttpRequest object is immutable.
                     */
                    final ServerHttpRequest theNewServerHttpRequest = inServerWebExchange
                        .getRequest()
                        .mutate()
                        .header(mRemoteAddressHeader, theRemoteAddress.toString())
                        .build();
        
                    /*
                     * In the same way as with the ServerHttpRequest a mutable wrapper also need
                     * to be created around the ServerWebExchange object in order to be able to
                     * set the new ServerHttpRequest on the ServerWebExchange.
                     */
                    theServerWebExchange = inServerWebExchange
                        .mutate()
                        .request(theNewServerHttpRequest)
                        .build();
                }
        
                return super.handle(theServerWebExchange);
            }
        
            public String getRemoteAddressHeader() {
                return mRemoteAddressHeader;
            }
        
            public void setRemoteAddressHeader(final String inRemoteAddressHeader) {
                Assert.hasText(inRemoteAddressHeader,
                    "Remote address header name must not be null or empty.");
                mRemoteAddressHeader = inRemoteAddressHeader;
            }
        }        
    
}
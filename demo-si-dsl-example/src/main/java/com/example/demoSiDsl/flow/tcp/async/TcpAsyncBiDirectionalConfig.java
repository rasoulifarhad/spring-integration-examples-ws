package com.example.demoSiDsl.flow.tcp.async;

/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpConnectionCloseEvent;
import org.springframework.integration.ip.tcp.connection.TcpConnectionOpenEvent;

import com.example.demoSiDsl.flow.tcp.async.TcpAsyncBiDirectionalConfig.SampleProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * Demonstrates independent bi-directional communication between peers.
 * One side opens the connection and they proceed to send messages to each
 * other on different schedules.
 * There are two client instances.
 *
 * @author Gary Russell
 * @since 5.3
 *
 */
@Configuration
@Profile("TcpAsyncBiDirectionalConfig")
@EnableConfigurationProperties(SampleProperties.class)
public class TcpAsyncBiDirectionalConfig {



	/**
	 * Properties for the sample.
	 *
	 * @author Gary Russell
	 * @since 5.3
	 *
	 */
	@ConfigurationProperties("tcp")
	public static class SampleProperties {
	
		private int serverPort;
	
		public int getServerPort() {
			return this.serverPort;
		}
	
		public void setServerPort(int serverPort) {
			this.serverPort = serverPort;
		}
	
	}
	
}

@Configuration
@Profile("TcpAsyncBiDirectionalConfig")
class ClientPeer {

	@Bean
	public AbstractClientConnectionFactory client1(SampleProperties properties) {
		return Tcp.netClient("localhost", properties.getServerPort()).get();
	}

	@Bean
	public IntegrationFlow client1Out(AbstractClientConnectionFactory client1) {
		return IntegrationFlows.fromSupplier(() -> "Hello from client1", e -> e.id("client1Adapter")
						.poller(Pollers.fixedDelay(3000)))
				.handle(Tcp.outboundAdapter(client1))
				.get();
	}

	@Bean
	public IntegrationFlow client1In(AbstractClientConnectionFactory client1) {
		return IntegrationFlows.from(Tcp.inboundAdapter(client1))
				.transform(Transformers.objectToString())
				.log(msg -> "client1: " + msg.getPayload())
				.get();
	}

	@Bean
	public AbstractClientConnectionFactory client2(SampleProperties properties) {
		return Tcp.netClient("localhost", properties.getServerPort()).get();
	}

	@Bean
	public IntegrationFlow client2Out(AbstractClientConnectionFactory client2) {
		return IntegrationFlows.fromSupplier(() -> "Hello from client2", e -> e.id("client2Adapter")
						.poller(Pollers.fixedDelay(2000)))
				.handle(Tcp.outboundAdapter(client2))
				.get();
	}

	@Bean
	public IntegrationFlow client2In(AbstractClientConnectionFactory client2) {
		return IntegrationFlows.from(Tcp.inboundAdapter(client2))
				.transform(Transformers.objectToString())
				.log(msg -> "client2: " + msg.getPayload())
				.get();
	}

}

@Configuration
@Profile("TcpAsyncBiDirectionalConfig")
class ServerPeer {

	private final Set<String> clients = ConcurrentHashMap.newKeySet();

	@Bean
	public AbstractServerConnectionFactory server(SampleProperties properties) {
		return Tcp.netServer(properties.getServerPort()).get();
	}

	@Bean
	public IntegrationFlow serverIn(AbstractServerConnectionFactory server) {
		return IntegrationFlows.from(Tcp.inboundAdapter(server))
				.transform(Transformers.objectToString())
				.log(msg -> "server: " + msg.getPayload())
				.get();
	}

	@Bean
	public IntegrationFlow serverOut(AbstractServerConnectionFactory server) {
		return IntegrationFlows.fromSupplier(() -> "seed", e -> e.poller(Pollers.fixedDelay(5000)))
				.split(this.clients, "iterator")
				.enrichHeaders(h -> h.headerExpression(IpHeaders.CONNECTION_ID, "payload"))
				.transform(p -> "Hello from server")
				.handle(Tcp.outboundAdapter(server))
				.get();
	}

	@EventListener
	public void open(TcpConnectionOpenEvent event) {
		if (event.getConnectionFactoryName().equals("server")) {
			this.clients.add(event.getConnectionId());
		}
	}

	@EventListener
	public void close(TcpConnectionCloseEvent event) {
		this.clients.remove(event.getConnectionId());
	}

}

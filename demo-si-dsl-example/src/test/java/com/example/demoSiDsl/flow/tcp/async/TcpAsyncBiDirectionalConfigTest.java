package com.example.demoSiDsl.flow.tcp.async;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@SpringIntegrationTest(noAutoStartup = { "client1Adapter", "client2Adapter" })
@DirtiesContext
@Profile("TcpAsyncBiDirectionalConfig")
class TcpAsyncBiDirectionalConfigTest {

	@Autowired
	@Qualifier("client1Adapter")
	private SourcePollingChannelAdapter adapter1;

	@Autowired
	@Qualifier("client2Adapter")
	private SourcePollingChannelAdapter adapter2;

	@Autowired
	@Qualifier("client1In.channel#0")
	private AbstractMessageChannel client1In;

	@Autowired
	@Qualifier("client2In.channel#0")
	private AbstractMessageChannel client2In;

	@Autowired
	@Qualifier("serverIn.channel#0")
	private AbstractMessageChannel serverIn;

	@Test
	void testBothReceive() throws InterruptedException {
		CountDownLatch serverLatch = new CountDownLatch(1);
		CountDownLatch client1Latch = new CountDownLatch(1);
		CountDownLatch client2Latch = new CountDownLatch(1);
		this.serverIn.addInterceptor(new ChannelInterceptor() {

			@Override
			@Nullable
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				serverLatch.countDown();
				return message;
			}

		});
		this.client1In.addInterceptor(new ChannelInterceptor() {

			@Override
			@Nullable
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				client1Latch.countDown();
				return message;
			}

		});
		this.client2In.addInterceptor(new ChannelInterceptor() {

			@Override
			@Nullable
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				client2Latch.countDown();
				return message;
			}

		});
		this.adapter1.start();
		this.adapter2.start();
		assertThat(serverLatch.await(10, TimeUnit.SECONDS)).isTrue();
		assertThat(client1Latch.await(10, TimeUnit.SECONDS)).isTrue();
		assertThat(client2Latch.await(10, TimeUnit.SECONDS)).isTrue();
	}

}


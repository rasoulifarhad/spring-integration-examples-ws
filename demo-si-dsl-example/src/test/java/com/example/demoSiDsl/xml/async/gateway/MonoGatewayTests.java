package com.example.demoSiDsl.xml.async.gateway;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.demoSiDsl.xml.async.gateway.MonoAsyncGatewayConfig.MathService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;


/**
 * @author Oleg Zhurakousky
 * @author Gary Russell
 *
 */
@ContextConfiguration(classes = MonoGatewayTests.TestConfig.class)
@ExtendWith(SpringExtension.class)
@DirtiesContext
public class MonoGatewayTests {

	private static final Log logger = LogFactory.getLog(MonoGatewayTests.class);

	@Autowired
	private MathGateway gateway;

	@Test
	public void testMonoGateway() throws Exception {
		Random random = new Random();
		int[] numbers = new int[100];
		int expectedResults = 0;
		for (int i = 0; i < 100; i++) {
			numbers[i] = random.nextInt(200);
			if (numbers[i] > 100) {
				expectedResults++;
			}
		}
		final CountDownLatch latch = new CountDownLatch(expectedResults);
		final AtomicInteger failures = new AtomicInteger();
		for (int i = 0; i < 100; i++) {
			final int number = numbers[i];
			gateway.multiplyByTwo(number)
					.subscribeOn(Schedulers.boundedElastic())
					.filter(p -> p != null)
					.doOnNext(result1 -> {
						logger.info("Result of multiplication of " + number + " by 2 is " + result1);
						latch.countDown();
					})
					.doOnError(t -> {
						failures.incrementAndGet();
						logger.error("Unexpected exception for " + number, t);
					}).subscribe();
		}
		assertTrue(latch.await(60, TimeUnit.SECONDS));
		assertThat(failures.get(), greaterThanOrEqualTo(0));
		logger.info("Finished");
	}

	@Configuration
	@EnableIntegration
	@ComponentScan
	@IntegrationComponentScan
	public static class TestConfig {

		@Bean
		@ServiceActivator(inputChannel = "mathServiceChannel")
		public MathService mathService() {
			return new MathService();
		}

	}

	@MessagingGateway(defaultReplyTimeout = "0")
	public interface MathGateway {

		@Gateway(requestChannel = "gatewayChannel")
		Mono<Integer> multiplyByTwo(int number);

	}

	@MessageEndpoint
	public static class Gt100Filter {

		@Filter(inputChannel = "gatewayChannel", outputChannel = "mathServiceChannel")
		public boolean filter(int i) {
			return i > 100;
		}

	}

}

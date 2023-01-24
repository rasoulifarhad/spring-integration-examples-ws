package com.example.demoSiDsl.xml.async.gateway;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.example.demoSiDsl.xml.async.gateway.FutureAsyncGatewayConfig.MathServiceGateway;

/**
 * @author Oleg Zhurakousky
 * @author Gary Russell
 *
 */
public class AsyncGatewayTest {
	private static Log logger = LogFactory.getLog(AsyncGatewayTest.class);
	private static ExecutorService executor = Executors.newFixedThreadPool(100);
	private static int timeout = 20;

	@Test
	public void testAsyncGateway() throws Exception{
		ConfigurableApplicationContext ac =
				new FileSystemXmlApplicationContext("src/main/resources/META-INF/spring/integration/*.xml");
		MathServiceGateway mathService = ac.getBean("mathService", MathServiceGateway.class);
		Map<Integer, Future<Integer>> results = new HashMap<Integer, Future<Integer>>();
		Random random = new Random();
		for (int i = 0; i < 100; i++) {
			int number = random.nextInt(200);
			Future<Integer> result = mathService.multiplyByTwo(number);
			results.put(number, result);
		}
		for (final Map.Entry<Integer, Future<Integer>> resultEntry : results.entrySet()) {
			executor.execute(() -> {
				int[] result = processFuture(resultEntry);

				if (result[1] == -1){
					logger.info("Multiplying " + result[0] + " should be easy. You should be able to multiply any number < 100 by 2 in your head");
				} else if (result[1] == -2){
					logger.info("Multiplication of " + result[0] + " by 2 is can not be accomplished in " + timeout + " seconds");
				} else {
					logger.info("Result of multiplication of " + result[0] + " by 2 is " + result[1]);
				}
			});
		}
		executor.shutdown();
		executor.awaitTermination(60, TimeUnit.SECONDS);
		logger.info("Finished");
		ac.close();
	}

	public static int[] processFuture(Map.Entry<Integer, Future<Integer>> resultEntry){
		int originalNumber = resultEntry.getKey();
		Future<Integer> result = resultEntry.getValue();
		try {
			int finalResult =  result.get(timeout, TimeUnit.SECONDS);
			return new int[]{originalNumber, finalResult};
		} catch (ExecutionException e) {
			return new int[]{originalNumber, -1};
		} catch (TimeoutException tex){
			return new int[]{originalNumber, -2};
		} catch (Exception ex){
			System.out.println();
			// ignore
		}
		return null;
	}
}

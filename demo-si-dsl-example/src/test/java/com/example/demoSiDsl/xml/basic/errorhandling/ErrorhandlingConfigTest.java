package com.example.demoSiDsl.xml.basic.errorhandling;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Demonstrates the handling of Exceptions in an asynchronous messaging
 * environment. View the 'errorHandlingDemo.xml' configuration file within
 * this same package. Notice the use of a &lt;header-enricher/&gt; element
 * within a &lt;chain/&gt; that establishes an 'error-channel' reference
 * prior to passing the message to a &lt;service-activator/&gt;.
 *
 * @author Iwein Fuld
 */
@Profile("ErrorhandlingConfig")
public class ErrorhandlingConfigTest {
    
    @Test
	public void runPartyDemoTest() throws Exception{
		new ClassPathXmlApplicationContext
		("/META-INF/com/example/demoSiDsl/xml/basic/errorhandling/errorHandlingDemo.xml",
		 ErrorhandlingConfigTest.class);

		Thread.sleep(5000);
	}    
}

package com.example.demoSiDsl.flow.log;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.scheduling.annotation.Scheduled;

import net.datafaker.Faker;

@Configuration
@Profile("LoggingFlowConfig")
public class LoggingFlowConfig implements ApplicationContextAware {

    ApplicationContext applicationContext;


    // @MessagingGateway(name = "myLoggerGateway" )
    @Profile("LoggingFlowConfig")
    public interface  LoggerGateway {
        void sendToLogger(String data);
    }

    @Bean
    @Profile("LoggingFlowConfig")
    public  IntegrationFlow loggingFlow() {
        return IntegrationFlows.from(LoggerGateway.class ,(gateway) -> gateway.beanName("loggingFlowLoggerGateway") )
                            .log(LoggingHandler.Level.DEBUG, "TEST_LOGGER",
                                   m -> m.getHeaders().getId() + ": " + m.getPayload())
                            .handle(payload -> System.out.println(payload) )
                            .get();
    }


    @Bean
    @Profile("LoggingFlowConfig")
    @DependsOn("loggingFlow")
    public Runner runner() {
        LoggerGateway loggerGateway =  (LoggerGateway) applicationContext.getBean("loggingFlowLoggerGateway");
        return new Runner(loggerGateway);
    }
    
    @Profile("LoggingFlowConfig")
    public static class Runner {

        private LoggerGateway loggerGateway;
        Faker faker = new Faker() ;

        public Runner(LoggerGateway loggerGateway) {
            this.loggerGateway = loggerGateway;
        }

        @Scheduled(fixedDelay = 60000)
        public void run() {

            loggerGateway.sendToLogger(faker.chuckNorris().fact());
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext ;
    }
    
}

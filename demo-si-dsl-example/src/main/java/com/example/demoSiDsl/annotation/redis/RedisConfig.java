package com.example.demoSiDsl.annotation.redis;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import net.datafaker.Faker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import redis.clients.jedis.JedisPoolConfig;


@Configuration
@Profile("RedisConfig")
public class RedisConfig {
    
    // @Value("${redis.host}")
    private String redisHost = "localhost";
 
    // @Value("${redis.port:6379}")
    private int redisPort=6379;    


    @Bean 
    @Profile("RedisConfig")
    public JedisPoolConfig poolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        return poolConfig;

    }

    @Bean 
    @Profile("RedisConfig")
    public JedisClientConfiguration clientConfiguration() {
        return JedisClientConfiguration.builder()
                           .usePooling()
                           .poolConfig(poolConfig())
                           .build()    ;
    }
    @Bean
    @Profile("RedisConfig")
    public  RedisStandaloneConfiguration standaloneConfiguration() {
        
        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
        conf.setHostName(redisHost);
        conf.setPort(redisPort);
        return conf;
    }

    @Bean
    @Profile("RedisConfig")
    public RedisConnectionFactory redisConnectionFactory() {
        
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(standaloneConfiguration(),clientConfiguration());
        return connectionFactory;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @Profile("RedisConfig")
    public static class PostPublishedEvent{
        private Integer id ;
        private String postUrl;
        private String postTitle;    
        private List<String> emails;
    }


    @Profile("RedisConfig")
    public interface RedisChannelGateway {
        void enqueue(PostPublishedEvent event);
    }

    @ImportResource({"redis-integration.xml","redis-queue-config.xml"})
    @AutoConfigureAfter(RedisConfig.class)
    @Configuration
    @Profile("RedisConfig")
    public static class SpringIntegrationConfig{


    }

    @Profile("RedisConfig")
    public interface QueueService {

        void enqueue(PostPublishedEvent event);
    }
     
    @Service("RedisQueueService")
    @Profile("RedisConfig")
    public static class RedisQueueService implements QueueService {
     
        private RedisChannelGateway channelGateway;
     
        @Autowired
        public RedisQueueService(RedisChannelGateway channelGateway) {
            this.channelGateway = channelGateway;
        }
     
        @Override
        public void enqueue(PostPublishedEvent event) {
            System.out.println("enqueue event : "+ event.toString());
            System.out.println();
            channelGateway.enqueue(event);
        }
    }
    
    // @Autowired
    // RedisQueueService   queueService ;

    @Bean
    @DependsOn("RedisQueueService")
    @Profile("RedisConfig")
    // public Runner runner(@Autowired QueueService queueService) {
    public Runner runner( QueueService queueService) {
            return new Runner(queueService);
    }
    
    @Profile("RedisConfig")
    public static class Runner {

        private final QueueService queueService;
        private Faker faker = new Faker();
        private AtomicInteger atomicInteger = new AtomicInteger(0);

        public Runner(QueueService queueService) {
            this.queueService = queueService;
        }

        @Scheduled(fixedDelay = 5000)
        public void run() {
            String[] arr = {faker.internet().emailAddress(),faker.internet().emailAddress()};

            PostPublishedEvent postPublishedEvent= new PostPublishedEvent();
            postPublishedEvent.setId(atomicInteger.incrementAndGet());
            postPublishedEvent.setPostUrl(faker.name().name());
            postPublishedEvent.setPostTitle(faker.address().cityName());
            postPublishedEvent.setEmails(Arrays.asList(arr));
            

            this.queueService.enqueue(postPublishedEvent);
        }

    }
    

    @Profile("RedisConfig")
    public interface EventProcessingService {
        void process(PostPublishedEvent event);
    }
     
    @Service("RedisEventProcessingService")
    @Profile("RedisConfig")
    public class RedisEventProcessingService implements EventProcessingService {
     
        @Override
        public void process(PostPublishedEvent event) {
            System.out.println("pooled event: "+ event.toString());
            System.out.println();
        }
     
    }     
	// @Bean
    //@Profile("RedisConfig")
	// public DirectChannel receiverChannel() {
	// 	return new DirectChannel();
	// }    
    // @Bean
    //@Profile("RedisConfig")
	// public RedisQueueMessageDrivenEndpoint consumerEndPoint() {
	// 	RedisQueueMessageDrivenEndpoint endPoint = new RedisQueueMessageDrivenEndpoint("Redis-Queue",
	// 			jedisConnectionFactory());
	// 	endPoint.setOutputChannelName("receiverChannel");
	// 	return endPoint;
	// }    

    // @MessagingGateway
    //@Profile("RedisConfig")
    // public interface MessagingGateway {
    //     @Gateway(requestChannel = "inputChannel")
    //     public <S> Future<S> sendMessage(S request);

    // }
    // @Component
    //@Profile("RedisConfig")
    // public static class MessageListener {

    //     @ServiceActivator(inputChannel = "inputChannel",
    //                        outputChannel = "redisChannel"
    //                          )
    //     public Message<?> receiveFromService(Message<?> message) {
    //         System.out.println("received from service");
    //         return message;
    //     }

    // }

    // @Data
    // @NoArgsConstructor
    //@Profile("RedisConfig")
    // public static class Student implements Serializable {
    //     private static final long serialVersionUID = 4528883226398538198L;

    //     private String id;
	//     private String firstName;
	//     private String lastName;
	//     private String age;
	//     private String gender;
    // }

    // @RestController
    // @RequestMapping("/api/student")
    //@Profile("RedisConfig")
    // public static class StudentController {

    //     @Autowired
	//     private MessageGateway messageGateway;

	//     @PostMapping
	//     public void sendStudentInformation(@RequestBody Student student) {
	// 	    messageGateway.sendMessage(student);
	//     }
    // }
}

package com.example.demoSiDsl.flow.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.redis.inbound.RedisQueueMessageDrivenEndpoint;
import org.springframework.messaging.MessageChannel;

import com.fasterxml.jackson.databind.JsonNode;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
@Profile("RedisConfigDsl2")
public class RedisConfigDsl2 {
    
    // @Value("${redis.host}")
    private String redisHost = "localhost";
 
    // @Value("${redis.port:6379}")
    private int redisPort=6379;    


    @Bean 
    @Profile("RedisConfigDsl2")
    public JedisPoolConfig poolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        return poolConfig;

    }

    @Bean 
    @Profile("RedisConfigDsl2")
    public JedisClientConfiguration clientConfiguration() {
        return JedisClientConfiguration.builder()
                           .usePooling()
                           .poolConfig(poolConfig())
                           .build()    ;
    }
    @Bean
    @Profile("RedisConfigDsl2")
    public  RedisStandaloneConfiguration standaloneConfiguration() {
        
        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
        conf.setHostName(redisHost);
        conf.setPort(redisPort);
        return conf;
    }

    @Bean
    @Profile("RedisConfigDsl2")
    public RedisConnectionFactory redisConnectionFactory() {
        
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(standaloneConfiguration(),clientConfiguration());
        return connectionFactory;
    }


    // I tested it with > lpush foo '{"foo":"bar"}' in redis-cli.


    @Bean
    @Profile("RedisConfigDsl2")
    public RedisQueueMessageDrivenEndpoint redisQueueMessageDrivenEndpoint(RedisConnectionFactory connectionFactory) {
        RedisQueueMessageDrivenEndpoint endpoint = new RedisQueueMessageDrivenEndpoint("foo", connectionFactory);
        Jackson2JsonRedisSerializer<? extends JsonNode> serializer = new Jackson2JsonRedisSerializer<>(JsonNode.class);
        endpoint.setSerializer(serializer);
        endpoint.setAutoStartup(true);
        endpoint.setOutputChannel(rpopChannel());
        return endpoint;
    }
    
    @Bean
    @Profile("RedisConfigDsl2")
    public IntegrationFlow flow(RedisConnectionFactory connectionFactory) {
        return IntegrationFlows.from(rpopChannel())
                .handle(System.out::println)
                .get();
    }
    
    @Bean
    @Profile("RedisConfigDsl2")
    public MessageChannel rpopChannel() {
        return new DirectChannel();
    }    


}

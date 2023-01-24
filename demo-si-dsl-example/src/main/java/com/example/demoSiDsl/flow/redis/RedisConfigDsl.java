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

import com.fasterxml.jackson.databind.JsonNode;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
@Profile("RedisConfigDsl")
public class RedisConfigDsl {
    
    // @Value("${redis.host}")
    private String redisHost = "localhost";
 
    // @Value("${redis.port:6379}")
    private int redisPort=6379;    


    @Bean 
    @Profile("RedisConfigDsl")
    public JedisPoolConfig poolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        return poolConfig;

    }

    @Bean 
    @Profile("RedisConfigDsl")
    public JedisClientConfiguration clientConfiguration() {
        return JedisClientConfiguration.builder()
                           .usePooling()
                           .poolConfig(poolConfig())
                           .build()    ;
    }
    @Bean
    @Profile("RedisConfigDsl")
    public  RedisStandaloneConfiguration standaloneConfiguration() {
        
        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
        conf.setHostName(redisHost);
        conf.setPort(redisPort);
        return conf;
    }

    @Bean
    @Profile("RedisConfigDsl")
    public RedisConnectionFactory redisConnectionFactory() {
        
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(standaloneConfiguration(),clientConfiguration());
        return connectionFactory;
    }


    @Bean
    @Profile("RedisConfigDsl")
    public RedisQueueMessageDrivenEndpoint redisQueueMessageDrivenEndpoint(RedisConnectionFactory connectionFactory) {
        RedisQueueMessageDrivenEndpoint endpoint = new RedisQueueMessageDrivenEndpoint("foo", connectionFactory);
        Jackson2JsonRedisSerializer<? extends JsonNode> serializer = new Jackson2JsonRedisSerializer<>(JsonNode.class);
        endpoint.setSerializer(serializer);
        endpoint.setAutoStartup(true);
        endpoint.setOutputChannel(new DirectChannel()); // will be replaced
        return endpoint;
    }
    
    @Bean
    @Profile("RedisConfigDsl")
    public IntegrationFlow flow(RedisConnectionFactory connectionFactory) {
        return IntegrationFlows.from(redisQueueMessageDrivenEndpoint(connectionFactory))
                .handle(System.out::println)
                .get();
    }    
    // I tested it with > lpush foo '{"foo":"bar"}' in redis-cli.


    // @Bean
    // @Profile("RedisConfigDsl")
    // public RedisQueueMessageDrivenEndpoint redisQueueMessageDrivenEndpoint(RedisConnectionFactory connectionFactory) {
    //     RedisQueueMessageDrivenEndpoint endpoint = new RedisQueueMessageDrivenEndpoint("foo", connectionFactory);
    //     Jackson2JsonRedisSerializer<? extends JsonNode> serializer = new Jackson2JsonRedisSerializer<>(JsonNode.class);
    //     endpoint.setSerializer(serializer);
    //     endpoint.setAutoStartup(true);
    //     endpoint.setOutputChannel(rpopChannel());
    //     return endpoint;
    // }
    
    // @Bean
    // @Profile("RedisConfigDsl")
    // public IntegrationFlow flow(RedisConnectionFactory connectionFactory) {
    //     return IntegrationFlows.from(rpopChannel())
    //             .handle(System.out::println)
    //             .get();
    // }
    
    // @Bean
    // @Profile("RedisConfigDsl")
    // public MessageChannel rpopChannel() {
    //     return new DirectChannel();
    // }    


    // @Bean
    // public RedisOutboundGateway redisOutboundGateway(RedisConnectionFactory connectionFactory) {
    //     RedisOutboundGateway gateway = new RedisOutboundGateway(connectionFactory);
    //     Jackson2JsonRedisSerializer<? extends JsonNode> serializer = new Jackson2JsonRedisSerializer<>(JsonNode.class);
    //     gateway.setArgumentsSerializer(serializer);
    //     return gateway;
    // }
    
    // @Bean
    // public IntegrationFlow redisLpushRequestFlow(RedisOutboundGateway gateway, BeanFactory beanFactory) {
    //     ExpressionArgumentsStrategy strategy = new ExpressionArgumentsStrategy(new String[]{"headers.queue", "#cmd == 'LPUSH' ? payload : null"}, true);
    //     strategy.setBeanFactory(beanFactory);
    //     gateway.setArgumentsStrategy(strategy);
    //     return flow -> flow.publishSubscribeChannel(s->s.subscribe(f -> f
    //             .enrich(e -> e.<ObjectNode>requestPayload(m -> {
    //                 String partition = m.getHeaders().get("correlationId").toString();
    //                 ObjectNode objectNode = m.getPayload();
    //                 objectNode.put(PayLoadKeys.PARTITION, partition);
    //                 objectNode.put(PayLoadKeys.SEQ, m.getHeaders().get("sequenceNumber").toString());
    //                 return objectNode;
    //             }).shouldClonePayload(false)
    //                     .header(RedisHeaders.COMMAND, "LPUSH").header("queue", files))
    //             .handle(gateway).channel("redisLpushResponseFlow.input")));
    // }
    
    // @Bean
    // public IntegrationFlow redisLpushResponseFlow() {
    //     return flow -> flow.resequence().aggregate().<List<Long>>handle((p,h)-> {
    //                 ObjectNode objectNode = mapper.createObjectNode();
    //                 objectNode.put(PayLoadKeys.PARTITION, h.get("correlationId").toString());
    //                 if(h.get("mode").equals("debug")) {
    //                     objectNode.set(PayLoadKeys.DEBUG,
    //                             mapper.valueToTree(p.stream().collect(Collectors.toList())));
    //                 }
    //                 return objectNode;
    //             }).channel(httpInboundReplyChannel());
    // @Bean
    // public MessageChannel redisRpopChannel() {
    //     return MessageChannels.queue().get();
    // }
    
    // @Bean(name = PollerMetadata.DEFAULT_POLLER)
    // public PollerMetadata poller() {
    //     return Pollers.fixedRate(500).get();
    // }
    
    // @Bean
    // public RedisQueueMessageDrivenEndpoint redisQueueMessageDrivenEndpoint(RedisConnectionFactory connectionFactory, BeanFactory beanFactory) {
    //     RedisQueueMessageDrivenEndpoint endpoint = new RedisQueueMessageDrivenEndpoint(files, connectionFactory);
    //     Jackson2JsonRedisSerializer<? extends JsonNode> serializer = new Jackson2JsonRedisSerializer<>(JsonNode.class);
    //     endpoint.setSerializer(serializer);
    //     endpoint.setBeanFactory(beanFactory);
    //     endpoint.setAutoStartup(true);
    //     endpoint.setOutputChannel(redisRpopChannel());
    //     endpoint.afterPropertiesSet();
    //     endpoint.start();
    //     return endpoint;
    // }
    
    // @Bean
    // public IntegrationFlow redisQueuePollingFlow() {
    
    //     class ThrottledTaskExecutor implements TaskExecutor {
    //         final Semaphore semaphore;
    //         final TaskExecutor taskExecutor;
    
    //         ThrottledTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
    //             this.taskExecutor = taskExecutor;
    //             this.semaphore = new Semaphore(taskExecutor.getCorePoolSize());
    //         }
    
    //         @Override
    //         public void execute(Runnable task) {
    //             if (task == null) {
    //                 throw new NullPointerException("Task is null in ThrottledTaskExecutor.");
    //             }
    //             doSubmit(task);
    //         }
    
    //         void doSubmit(final Runnable task) {
    //             try {
    //                 semaphore.acquire();
    //             } catch (InterruptedException e) {
    //                 Thread.currentThread().interrupt();
    //                 throw new TaskRejectedException("Task could not be submitted because of a thread interruption.");
    //             }
    //             try {
    //                 taskExecutor.execute(new FutureTask<Void>(task, null) {
    
    //                     @Override
    //                     protected void done() {
    //                         semaphore.release();
    //                     }
    //                 });
    //             } catch (TaskRejectedException e) {
    //                 semaphore.release();
    //                 throw e;
    //             }
    //         }
    //     }
    
    //     return IntegrationFlows
    //             .from(redisRpopChannel())
    //             .transform(Transformers.fromJson(ObjectNode.class))
    //             .handle(message -> {
    //                 ObjectNode p = (ObjectNode) message.getPayload();
    //                 ThreadPoolTaskExecutor taskExecutor = taskExecutor();
    //                 ThrottledTaskExecutor throttledTaskExecutor = new ThrottledTaskExecutor(taskExecutor);
    //                 if(p.hasNonNull(PayLoadKeys.ID_ARRAY)) {
    //                     String array = p.remove(PayLoadKeys.ID_ARRAY).asText();
    //                     if (p.hasNonNull(array)) {
    //                         p.remove(array).forEach(id -> {
    //                             ObjectNode param = p.deepCopy();
    //                             final Long finalId = id.asLong();
    //                             param.put("id", finalId);
    //                             throttledTaskExecutor.execute(new JobLaunchTask(param));
    //                         });
    //                     }
    //                 } else {
    //                     throttledTaskExecutor.execute(new JobLaunchTask(p));
    //                 }
    //                 taskExecutor.shutdown();
    //             }).get();
    // }    
}

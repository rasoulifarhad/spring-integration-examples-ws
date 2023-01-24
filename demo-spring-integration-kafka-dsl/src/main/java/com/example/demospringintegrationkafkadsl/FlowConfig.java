package com.example.demospringintegrationkafkadsl;

import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

// import java.util.Map;
// import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
// import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
// import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.integration.kafka.dsl.Kafka;

@Configuration
@EnableConfigurationProperties(KafkaAppProperties.class)
public class FlowConfig {
    
    @Autowired
    private KafkaAppProperties kafkaAppProperties;

    
    @Bean
    public IntegrationFlow toKafka(KafkaTemplate<?, ?> kafkaTemplate) {
        return f -> f
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate)
                        .messageKey(this.kafkaAppProperties.getMessageKey()));
    }

    @Bean
    public IntegrationFlow fromKafkaFlow(ConsumerFactory<?, ?> consumerFactory) {
        return IntegrationFlows
                .from(Kafka.messageDrivenChannelAdapter(consumerFactory, this.kafkaAppProperties.getTopic()))
                .channel(c -> c.queue("fromKafka"))
                .get();
    }

    // @Autowired
    // private KafkaProperties kafkaProperties;

    // @Bean
    // public IntegrationFlow addAnotherListenerForTopics(String... topics) {
    //     Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
    //     // change the group id so we don't revoke the other partitions.
    //     consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG,
    //             consumerProperties.get(ConsumerConfig.GROUP_ID_CONFIG) + "x");
    //     IntegrationFlow flow =
    //         IntegrationFlows
    //             .from(Kafka.messageDrivenChannelAdapter(
    //                     new DefaultKafkaConsumerFactory<String, String>(consumerProperties), this.properties.getNewTopic()))
    //             .channel("fromKafka")
    //             .get();
    //     return flow ;
    // }


}

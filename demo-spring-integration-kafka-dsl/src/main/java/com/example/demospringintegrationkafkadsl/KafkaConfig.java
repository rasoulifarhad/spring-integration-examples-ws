package com.example.demospringintegrationkafkadsl;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafkaAppProperties.class)
public class KafkaConfig {
    
    /*
        * Boot's autoconfigured KafkaAdmin will provision the topics.
        */

    @Bean
    public NewTopic topic(KafkaAppProperties kafkaAppProperties) {
        return new NewTopic(kafkaAppProperties.getTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic newTopic(KafkaAppProperties kafkaAppProperties) {
        return new NewTopic(kafkaAppProperties.getNewTopic(), 1, (short) 1);
    }

}

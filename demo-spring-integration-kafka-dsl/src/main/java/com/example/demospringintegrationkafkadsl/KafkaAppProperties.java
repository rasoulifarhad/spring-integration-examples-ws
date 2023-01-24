package com.example.demospringintegrationkafkadsl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Properties for the kafka sample app.
 *
 * @author Gary Russell
 * @since 5.0
 *
 */
@Configuration
@ConfigurationProperties("kafka")
public class KafkaAppProperties {

	private String topic;

	private String newTopic;

	private String messageKey;

	public String getTopic() {
		return this.topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getNewTopic() {
		return this.newTopic;
	}

	public void setNewTopic(String newTopic) {
		this.newTopic = newTopic;
	}

	public String getMessageKey() {
		return this.messageKey;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

}
package com.bigpanda.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BlackBoxKafkaMessagesListenerVerticle extends AbstractVerticle {
    private Logger logger = LoggerFactory.getLogger(getClass());


    private KafkaConsumer<String, String> consumer;
    private Properties kafkaConfig;
    private String topic;

    @Override
    public void start() throws Exception {
        consumer = KafkaConsumer.create(vertx, kafkaConfig);
        consumer.handler(event -> {
            System.out.println(event.record());
        });
        consumer.subscribe(topic);
    }

    private Map<String, String> prepareConfig() {
        Map<String, String> config = new HashMap<>(config().size());
        config().forEach(e -> config.put(e.getKey(), e.getValue().toString()));
        return config;
    }

    @Override
    public void stop() throws Exception {
    	consumer.close(result -> {
    		if (result.succeeded()) {
    			logger.info("kafka consumer closed");
			} else {
				logger.info("kafka consumer closing failed");
			}
    	});
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setKafkaConfig(Properties kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }
}

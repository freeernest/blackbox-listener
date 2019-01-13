package com.bigpanda.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BlackBoxKafkaMessagesListenerVerticle extends AbstractVerticle {
    private Logger logger = LoggerFactory.getLogger(getClass());


    private KafkaConsumer<String, String> consumer;
    private Properties kafkaConfig;
    private String topic;
    AsyncMap<String, Integer> dataStatisticsMap;
    AsyncMap<String, Integer> typeStatisticsMap;

    @Override
    public void start() throws Exception {
        consumer = KafkaConsumer.create(vertx, kafkaConfig);

        //Set<HazelcastInstance> instances = Hazelcast.getAllHazelcastInstances();
        //HazelcastInstance hz = instances.stream().findFirst().get();
        //dataStatisticsMap = hz.getMap("dataStatisticsMap");
        //typeStatisticsMap = hz.getMap("typeStatisticsMap");

        vertx.sharedData().<String, Integer>getClusterWideMap("dataStatisticsMap",
                res -> {
                    dataStatisticsMap = res.result();
                });
        vertx.sharedData().<String, Integer>getClusterWideMap("typeStatisticsMap",
                res -> {
                    typeStatisticsMap = res.result();
                });

        consumer.handler(event -> {

            String message = event.record().value();
            System.out.println(message);
            JSONObject jsonObj = null;
            String data = null;
            String event_type = null;
            try {
                jsonObj = new JSONObject(message);
                data = jsonObj.get("data").toString();
                event_type = jsonObj.get("event_type").toString();
            } catch (JSONException e) {
                e.printStackTrace(); //should never happen
            }

            final String finalEvent_type = event_type;
            typeStatisticsMap.get(event_type, res->{
                Integer oldCounterValue = res.result();
                Integer newCounterValue = 1;

                if (oldCounterValue != null) {
                    newCounterValue = ++oldCounterValue;
                }
                typeStatisticsMap.put(finalEvent_type, newCounterValue, resPut->{
                    if (resPut.succeeded()) {
                        logger.info("Added data into the type statistics map {} ", Json.encodePrettily(finalEvent_type));
                    } else {
                        logger.debug("Failed to add data into the type statistics map {} ", Json.encodePrettily(finalEvent_type));
                    }
                });
            });

            List<String> splittedData = Arrays.asList(data.split(" "));
            splittedData.forEach(w -> {

               dataStatisticsMap.get(w, res->{
                   Integer oldCounterValue = res.result();
                   Integer newCounterValue = 1;

                    if (oldCounterValue != null) {
                        newCounterValue = ++oldCounterValue;
                    }
                    dataStatisticsMap.put(w, newCounterValue, resPut->{
                        if (resPut.succeeded()) {
                            logger.info("Added data into the data statistics map {} ", Json.encodePrettily(w));
                        } else {
                            logger.debug("Failed to add data into the data statistics map {} ", Json.encodePrettily(w));
                        }
                    });
                });

            });

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

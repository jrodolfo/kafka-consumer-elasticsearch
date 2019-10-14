package com.jrodolfo.elasticsearch;

import com.jrodolfo.elasticsearch.util.PropertyUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Properties;

class ConsumerKafka {

    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private String bootstrapServer;
    private String groupId;
    private String enableAutoCommitConfig;
    private String maxPollRecordsConfig;

    ConsumerKafka() {
        Properties properties = PropertyUtil.getProperties();
        bootstrapServer = properties.getProperty("kafka.bootstrap.server");
        groupId = properties.getProperty("kafka.groupId");
        enableAutoCommitConfig = properties.getProperty("kafka.enableAutoCommitConfig");
        maxPollRecordsConfig = properties.getProperty("kafka.maxPollRecordsConfig");
    }

    KafkaConsumer<String, String> createKafkaConsumer(String topic) {
        Properties properties = new Properties();
        String stringName = StringDeserializer.class.getName();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, stringName);
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, stringName);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // if we want to control the commit of offsets - i.e. if we want to know when these
        // offsets get committed manually, make sure enableAutoCommitConfig is set to false
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommitConfig);
        // let's have a small number of records in each poll request
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecordsConfig);
        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        // subscribe consumer to our topic(s)
        consumer.subscribe(Arrays.asList(topic));
        return consumer;
    }
}

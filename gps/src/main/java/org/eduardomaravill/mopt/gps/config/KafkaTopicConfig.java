package org.eduardomaravill.mopt.gps.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.util.unit.DataSize;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class KafkaTopicConfig {

    private static final int PARTITIONS = 3;
    private static final int REPLICAS = 1;
    private static final Long RETENTION_DURATION_DAYS = 8L;
    private static final Long MAX_SEGMENT_SIZE_IN_GB = 1L;
    private static final Long MAX_MESSAGE_SIZE_IN_MB = 1L;

    @Bean
    public NewTopic generateTopic(){
        Map<String, String> configurations = new HashMap<>();
        configurations.put(TopicConfig.CLEANUP_POLICY_CONFIG,TopicConfig.CLEANUP_POLICY_DELETE);
        configurations.put(TopicConfig.RETENTION_MS_CONFIG,
                String.valueOf(TimeUnit.DAYS.toMillis(RETENTION_DURATION_DAYS)));
        configurations.put(TopicConfig.SEGMENT_BYTES_CONFIG,
                String.valueOf(DataSize.ofGigabytes(MAX_SEGMENT_SIZE_IN_GB).toBytes()));
        configurations.put(TopicConfig.MAX_MESSAGE_BYTES_CONFIG,
                String.valueOf(DataSize.ofMegabytes(MAX_MESSAGE_SIZE_IN_MB).toBytes()));


        return TopicBuilder.name("GpsData")
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .configs(configurations)
                .build();
    }
}
package org.eduardomaravill.mopt.gpsconsumer.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eduardomaravill.mopt.gpsconsumer.dtos.GpsEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, GpsEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "gps-group");

        // ==================== OPTIMIZACIONES PARA ALTO THROUGHPUT ====================

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);           // Bajamos un poco (más estable)
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 65536);          // 64KB
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 200);          // Más rápido

        // Configuraciones importantes de estabilidad
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 45000);       // 45 segundos
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 15000);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Deshabilitamos auto-commit porque usamos batch + ack manual
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // Deserializador
        JacksonJsonDeserializer<GpsEvent> jsonDeserializer = new JacksonJsonDeserializer<>(GpsEvent.class);
        jsonDeserializer.setRemoveTypeHeaders(false);
        jsonDeserializer.addTrustedPackages("*");
        jsonDeserializer.setUseTypeMapperForKey(false);
        jsonDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                jsonDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GpsEvent> batchFactory(
            ConsumerFactory<String, GpsEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, GpsEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);

        // === Lo más importante para mejorar throughput ===
        factory.setConcurrency(6);                    // Sube según tu CPU (6-8 recomendado)
        factory.setAutoStartup(true);

        // Ack manual después de procesar el batch completo
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.BATCH
        );

        return factory;
    }
}
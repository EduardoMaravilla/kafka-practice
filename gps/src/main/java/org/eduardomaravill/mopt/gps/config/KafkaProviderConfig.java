package org.eduardomaravill.mopt.gps.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eduardomaravill.mopt.gps.dtos.GpsEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProviderConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, GpsEvent> producerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        // ==================== OPTIMIZACIONES PARA ALTO THROUGHPUT ====================

        // Confirmación rápida (mejor balance para alto volumen)
        props.put(ProducerConfig.ACKS_CONFIG, "1");

        // Batching - Más agresivo
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 131072);     // 64KB (mejor que 32KB)
        props.put(ProducerConfig.LINGER_MS_CONFIG, 15);         // 10ms (mejor que 5ms)

        // Compresión (lz4 suele ser más rápido que snappy en la mayoría de casos modernos)
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");

        // Memoria del buffer del producer
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 128 * 1024 * 1024); // 64MB

        // Máximo de requests en vuelo (permite más paralelismo)
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Tamaño máximo de request
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 2097152); // 1MB

        // Retries (para pruebas de estrés se puede bajar)
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, GpsEvent> kafkaTemplate(
            ProducerFactory<String, GpsEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
package org.eduardomaravill.mopt.gpsconsumer.services;

import lombok.RequiredArgsConstructor;
import org.eduardomaravill.mopt.gpsconsumer.dtos.GpsEvent;
import org.eduardomaravill.mopt.gpsconsumer.models.GpsEventEntity;
import org.eduardomaravill.mopt.gpsconsumer.repository.IGpsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GpsDataConsumer {

    private final IGpsRepository gpsRepository;

    @KafkaListener(
            topics = "GpsData",
            groupId = "gps-group",
            containerFactory = "batchFactory"
    )
    public void consumeBatch(List<GpsEvent> events) {
        // Convertimos Records a Entities
        List<GpsEventEntity> entities = events.stream()
                .map(event -> new GpsEventEntity(
                        event.deviceId(),
                        event.latitude(),
                        event.longitude(),
                        Instant.ofEpochMilli(event.timestamp())
                )).toList();

        // Guardado masivo (Batch)
        gpsRepository.saveAll(entities);

        System.out.println("💾 Guardados " + entities.size() + " registros en TimescaleDB");
    }
}

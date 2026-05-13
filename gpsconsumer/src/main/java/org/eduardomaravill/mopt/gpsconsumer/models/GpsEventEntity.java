package org.eduardomaravill.mopt.gpsconsumer.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "gps_events")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GpsEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;
    private Double latitude;
    private Double longitude;

    @Column(name = "event_timestamp")
    private Instant eventTimestamp;

    public GpsEventEntity(String deviceId, double latitude, double longitude, Instant instant) {
        this.deviceId = deviceId;
        this.latitude = latitude;
        this.longitude = longitude;
        eventTimestamp = instant;
    }
}

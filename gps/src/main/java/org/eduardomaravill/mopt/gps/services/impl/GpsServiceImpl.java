/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.eduardomaravill.mopt.gps.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eduardomaravill.mopt.gps.dtos.GpsEvent;
import org.eduardomaravill.mopt.gps.services.IGpsService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

/**
 *
 * @author eduar
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GpsServiceImpl implements IGpsService {

    private final KafkaTemplate<String, GpsEvent> gpsKafkaTemplate;

    @Override
    public void sendEvent(GpsEvent gpsEvent) {
        try {
            gpsKafkaTemplate.send("GpsData", gpsEvent.deviceId(),gpsEvent).get();
        } catch (InterruptedException e) {
            log.error("Error sending event", e);
        } catch (ExecutionException e) {
            log.error("Error sending event", e);
        }
    }
}

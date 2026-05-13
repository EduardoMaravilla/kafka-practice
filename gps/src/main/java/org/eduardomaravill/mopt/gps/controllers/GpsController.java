/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.eduardomaravill.mopt.gps.controllers;

import lombok.RequiredArgsConstructor;
import org.eduardomaravill.mopt.gps.dtos.GpsEvent;
import org.eduardomaravill.mopt.gps.services.IGpsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author eduar
 */
@RestController
@RequestMapping("/gps")
@RequiredArgsConstructor
public class GpsController {

    private final IGpsService gpsService;

    @PostMapping("/ingest")
    public ResponseEntity<Void> ingestDataGps(@RequestBody GpsEvent gpsEvent){
        gpsService.sendEvent(gpsEvent);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/thread")
    public String thread() {
        return Thread.currentThread().toString();
    }
}

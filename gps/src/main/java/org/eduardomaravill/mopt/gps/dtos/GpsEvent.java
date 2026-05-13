/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package org.eduardomaravill.mopt.gps.dtos;

/**
 *
 * @author eduar
 */
public record GpsEvent(
        String deviceId,
        double latitude,
        double longitude,
        long timestamp
        ) {
}

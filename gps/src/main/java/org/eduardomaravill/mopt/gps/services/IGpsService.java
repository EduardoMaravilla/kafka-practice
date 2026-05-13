/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.eduardomaravill.mopt.gps.services;

import org.eduardomaravill.mopt.gps.dtos.GpsEvent;

/**
 *
 * @author eduar
 */
public interface IGpsService {

    void sendEvent(GpsEvent gpsEvent);
}

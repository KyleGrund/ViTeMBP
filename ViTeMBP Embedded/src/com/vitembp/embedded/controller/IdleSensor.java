/*
 * Video Telemetry for Mountain Bike Platform back-end services.
 * Copyright (C) 2017 Kyle Grund
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vitembp.embedded.controller;

import com.vitembp.embedded.hardware.HardwareInterface;
import com.vitembp.embedded.hardware.Sensor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Provides sensor updates when idle.
 */
class IdleSensor  implements ControllerState {
    /**
     * Callback used to notify listener of sensors being added/removed
     */
    private Consumer<Set<String>> sensorsChangedCallback = null;
    
    /**
     * Callback used to notify listeners of a new senor reading.
     */
    private Consumer<Map<String, String>> sensorsReadCallback = null;
    
    /**
     * Set of currently enumerated sensors.
     */
    private final Set<String> sensors = new HashSet<>();
    
    @Override
    public Class execute(ExecutionContext state) {
        // get sensors for both checks
        Map<String, Sensor> currentSensors = HardwareInterface.getInterface().getSensors();
        
        // check for new sensors
        if (this.sensorsChangedCallback != null) {
            if (!currentSensors.keySet().equals(this.sensors)) {
                this.sensors.clear();
                this.sensors.addAll(currentSensors.keySet());
                this.sensorsChangedCallback.accept(this.sensors);
            }
        }
        
        // get readings for sensors
        if (this.sensorsReadCallback != null) {
            Map<String, String> readings = new HashMap<>();
            currentSensors.forEach((name, sensor) -> {
                if (sensor != null) {
                    readings.put(name, sensor.readSample());
                }
            });
            this.sensorsReadCallback.accept(readings);
        }
        
        // return to wait for start state to await start keypress
        return WaitForStart.class;
    }
    
    /**
     * Sets the Consumer which is called when sensors are added or removed.
     * @param callback The Consumer which is called when sensors are added or removed.
     */
    public void setSensorsChangedCallback(Consumer<Set<String>> callback) {
        this.sensorsChangedCallback = callback;
    }
    
    /**
     * Sets the Consumer which is called when a sensor reading is performed.
     * @param callback The Consumer which is called when a sensor reading is performed.
     */
    public void setSensorsReadCallback(Consumer<Map<String, String>> callback) {
        this.sensorsReadCallback = callback;
    }
}
